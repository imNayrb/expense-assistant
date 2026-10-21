package com.imnayrb.expenseassistant.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.imnayrb.expenseassistant.assistant.llm.ContentBlock;
import com.imnayrb.expenseassistant.assistant.llm.Tool;
import com.imnayrb.expenseassistant.expense.Category;
import com.imnayrb.expenseassistant.expense.ExpenseService;
import com.imnayrb.expenseassistant.expense.dto.ExpenseRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The tools Claude can use during a chat, and their execution against {@link ExpenseService}.
 *
 * <p>Claude never touches the database: it asks for a tool call, we run it with our own validation and send the
 * result back as a {@code tool_result}. Errors are returned to Claude as {@code is_error} results, so it can
 * explain the problem or retry with different arguments instead of failing the whole request.
 */
@Component
public class ExpenseTools {

    static final String LIST_EXPENSES = "list_expenses";
    static final String GET_SUMMARY = "get_summary";
    static final String ADD_EXPENSE = "add_expense";

    private final ExpenseService expenses;
    private final ObjectMapper mapper;
    private final List<Tool> definitions = buildDefinitions();

    public ExpenseTools(ExpenseService expenses, ObjectMapper mapper) {
        this.expenses = expenses;
        this.mapper = mapper;
    }

    public List<Tool> definitions() {
        return definitions;
    }

    /** Runs one {@code tool_use} block and returns the matching {@code tool_result} block. */
    public ContentBlock execute(ContentBlock toolUse) {
        try {
            Object result = switch (toolUse.name()) {
                case LIST_EXPENSES -> expenses.list(
                        date(toolUse.input(), "from"), date(toolUse.input(), "to"), category(toolUse.input()));
                case GET_SUMMARY -> expenses.summary(date(toolUse.input(), "from"), date(toolUse.input(), "to"));
                case ADD_EXPENSE -> expenses.create(toRequest(toolUse.input()));
                default -> throw new IllegalArgumentException("Unknown tool: " + toolUse.name());
            };
            return ContentBlock.toolResult(toolUse.id(), mapper.writeValueAsString(result), false);
        } catch (RuntimeException | JsonProcessingException e) {
            return ContentBlock.toolResult(toolUse.id(), "Error: " + e.getMessage(), true);
        }
    }

    private static ExpenseRequest toRequest(JsonNode input) {
        String description = required(input, "description").asText();
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        BigDecimal amount = new BigDecimal(required(input, "amount").asText());
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        JsonNode currency = input.get("currency");
        return new ExpenseRequest(
                description,
                amount,
                currency == null || currency.isNull() ? null : currency.asText(),
                Category.fromCode(required(input, "category").asText()),
                LocalDate.parse(required(input, "date").asText()));
    }

    private static LocalDate date(JsonNode input, String field) {
        JsonNode node = input == null ? null : input.get(field);
        return node == null || node.isNull() || node.asText().isBlank() ? null : LocalDate.parse(node.asText());
    }

    private static Category category(JsonNode input) {
        JsonNode node = input == null ? null : input.get("category");
        return node == null || node.isNull() || node.asText().isBlank() ? null : Category.valueOf(node.asText());
    }

    private static JsonNode required(JsonNode input, String field) {
        JsonNode node = input == null ? null : input.get(field);
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException("missing field '" + field + "'");
        }
        return node;
    }

    private static List<Tool> buildDefinitions() {
        ObjectNode list = JsonSchemas.object();
        JsonSchemas.dateProperty(list, "from", "First day of the range, inclusive.", true);
        JsonSchemas.dateProperty(list, "to", "Last day of the range, inclusive.", true);
        JsonSchemas.categoryProperty(list, "category", false);

        ObjectNode summary = JsonSchemas.object();
        JsonSchemas.dateProperty(summary, "from", "First day of the range, inclusive.", true);
        JsonSchemas.dateProperty(summary, "to", "Last day of the range, inclusive.", true);

        ObjectNode add = JsonSchemas.object();
        JsonSchemas.property(add, "description", "string", "Short description, in Italian.", true);
        JsonSchemas.property(add, "amount", "number", "Positive amount.", true);
        JsonSchemas.property(add, "currency", "string", "ISO 4217 code, default EUR.", false);
        JsonSchemas.categoryProperty(add, "category", true);
        JsonSchemas.dateProperty(add, "date", "Date of the expense.", true);

        return List.of(
                new Tool(LIST_EXPENSES,
                        "List the user's expenses in a date range, newest first, optionally for one category.", list),
                new Tool(GET_SUMMARY,
                        "Total spent in a date range, with totals per category. Prefer this for 'how much' questions.",
                        summary),
                new Tool(ADD_EXPENSE,
                        "Save a new expense. Use it only when the user explicitly asks to record an expense.", add));
    }
}
