package com.imnayrb.expenseassistant.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.imnayrb.expenseassistant.assistant.llm.ContentBlock;
import com.imnayrb.expenseassistant.assistant.llm.LlmClient;
import com.imnayrb.expenseassistant.assistant.llm.LlmException;
import com.imnayrb.expenseassistant.assistant.llm.Message;
import com.imnayrb.expenseassistant.assistant.llm.MessageRequest;
import com.imnayrb.expenseassistant.assistant.llm.MessageResponse;
import com.imnayrb.expenseassistant.assistant.llm.Tool;
import com.imnayrb.expenseassistant.assistant.llm.ToolChoice;
import com.imnayrb.expenseassistant.config.AnthropicProperties;
import com.imnayrb.expenseassistant.expense.Category;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Trasforma una frase come "ieri 30€ di benzina" in un {@link ExpenseDraft}.
 *
 * <p>Invece di chiedere a Claude un JSON come testo libero, definiamo un tool {@code record_expense} e lo
 * obblighiamo a chiamarlo ({@code tool_choice}). Lo JSON Schema del tool vincola l'output: otteniamo dati
 * strutturati da validare, non testo da interpretare.
 */
@Service
public class ExpenseParser {

    static final String TOOL_NAME = "record_expense";

    private final LlmClient llm;
    private final AnthropicProperties properties;
    private final Clock clock;
    private final Tool recordExpenseTool = buildTool();

    public ExpenseParser(LlmClient llm, AnthropicProperties properties, Clock clock) {
        this.llm = llm;
        this.properties = properties;
        this.clock = clock;
    }

    public ExpenseDraft parse(String text) {
        LocalDate today = LocalDate.now(clock);
        MessageRequest request = new MessageRequest(
                properties.model(),
                properties.maxTokens(),
                systemPrompt(today),
                List.of(Message.user(text)),
                List.of(recordExpenseTool),
                ToolChoice.tool(TOOL_NAME));

        MessageResponse response = llm.createMessage(request);
        ContentBlock toolUse = response.toolUses().stream()
                .filter(block -> TOOL_NAME.equals(block.name()))
                .findFirst()
                .orElseThrow(() -> new LlmException("Claude non ha restituito una spesa strutturata"));
        return toDraft(toolUse.input(), today);
    }

    ExpenseDraft toDraft(JsonNode input, LocalDate today) {
        if (input == null || !input.isObject()) {
            throw new LlmException("Claude ha restituito un input del tool non valido");
        }
        BigDecimal amount = parseAmount(input.get("amount"));
        if (amount == null || amount.signum() <= 0) {
            throw new UnparseableExpenseException("Non ho trovato un importo valido nel testo");
        }
        String description = text(input, "description");
        if (description == null) {
            throw new UnparseableExpenseException("Non ho capito per cosa è stata fatta la spesa");
        }
        Category category = Category.fromCode(text(input, "category"));
        String currency = text(input, "currency");
        currency = currency == null || !currency.matches("[A-Za-z]{3}") ? "EUR" : currency.toUpperCase(Locale.ROOT);
        return new ExpenseDraft(description, amount, currency, category, category.label(),
                parseDate(text(input, "date"), today));
    }

    private static BigDecimal parseAmount(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            // Accetta sia 12.5 sia "12,50": lo schema chiede un numero, ma meglio essere tolleranti.
            return new BigDecimal(node.asText().trim().replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseDate(String value, LocalDate fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }

    private static String text(JsonNode input, String field) {
        JsonNode node = input.get(field);
        if (node == null || node.isNull() || node.asText().isBlank()) {
            return null;
        }
        return node.asText().trim();
    }

    /** Il prompt è in inglese (rende meglio con il modello), ma chiede descrizioni in italiano. */
    static String systemPrompt(LocalDate today) {
        return """
                You extract exactly one expense from a short message written by the user, usually in Italian.
                Today is %s (%s).

                Rules:
                - Resolve relative dates ("oggi", "ieri", "l'altro ieri", "lunedì scorso") against today.
                  If no date is mentioned, use today.
                - Amounts may be written as "30€", "30 euro", "12,50": return a decimal number.
                - Currency is EUR unless another one is explicitly mentioned.
                - Write a short description in Italian, with a capital letter (e.g. "Benzina", "Cena al ristorante").
                - Choose the closest category; use OTHER only if nothing fits.
                - Always answer by calling the %s tool.
                """.formatted(today, today.getDayOfWeek(), TOOL_NAME);
    }

    private static Tool buildTool() {
        ObjectNode schema = JsonSchemas.object();
        JsonSchemas.property(schema, "description", "string", "Short description of the expense, in Italian.", true);
        JsonSchemas.property(schema, "amount", "number", "Amount spent, as a positive decimal number.", true);
        JsonSchemas.property(schema, "currency", "string", "ISO 4217 currency code, e.g. EUR.", true);
        JsonSchemas.categoryProperty(schema, "category", true);
        JsonSchemas.dateProperty(schema, "date", "Date of the expense.", true);
        return new Tool(TOOL_NAME, "Record one expense described by the user.", schema);
    }
}
