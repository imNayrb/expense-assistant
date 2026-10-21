package com.imnayrb.expenseassistant.assistant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imnayrb.expenseassistant.assistant.llm.ContentBlock;
import com.imnayrb.expenseassistant.assistant.llm.LlmException;
import com.imnayrb.expenseassistant.assistant.llm.MessageRequest;
import com.imnayrb.expenseassistant.assistant.llm.MessageResponse;
import com.imnayrb.expenseassistant.config.AnthropicProperties;
import com.imnayrb.expenseassistant.expense.Category;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ExpenseParserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");
    private static final Clock CLOCK = Clock.fixed(ZonedDateTime.of(2026, 10, 21, 10, 0, 0, 0, ROME).toInstant(), ROME);
    private static final AnthropicProperties PROPS =
            new AnthropicProperties("test-key", "https://api.anthropic.com", "claude-haiku-5-5", 1024, "2023-06-01");

    private final AtomicReference<MessageRequest> lastRequest = new AtomicReference<>();

    private ExpenseParser parserReturning(String toolInputJson) {
        return new ExpenseParser(request -> {
            lastRequest.set(request);
            try {
                return new MessageResponse("msg_1",
                        List.of(ContentBlock.toolUse("toolu_1", ExpenseParser.TOOL_NAME, MAPPER.readTree(toolInputJson))),
                        "tool_use");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, PROPS, CLOCK);
    }

    @Test
    void turnsToolInputIntoADraft() {
        ExpenseParser parser = parserReturning("""
                {"description":"Benzina","amount":30,"currency":"eur","category":"FUEL","date":"2026-10-20"}
                """);

        ExpenseDraft draft = parser.parse("ieri 30€ di benzina");

        assertThat(draft.description()).isEqualTo("Benzina");
        assertThat(draft.amount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(draft.currency()).isEqualTo("EUR");
        assertThat(draft.category()).isEqualTo(Category.FUEL);
        assertThat(draft.categoryLabel()).isEqualTo("Carburante");
        assertThat(draft.date()).isEqualTo(LocalDate.of(2026, 10, 20));
    }

    @Test
    void forcesTheRecordExpenseToolAndTellsClaudeTodaysDate() {
        parserReturning("""
                {"description":"Caffè","amount":1.2,"currency":"EUR","category":"RESTAURANTS","date":"2026-10-21"}
                """).parse("caffè 1,20");

        MessageRequest request = lastRequest.get();
        assertThat(request.toolChoice().type()).isEqualTo("tool");
        assertThat(request.toolChoice().name()).isEqualTo(ExpenseParser.TOOL_NAME);
        assertThat(request.tools()).extracting("name").containsExactly(ExpenseParser.TOOL_NAME);
        assertThat(request.system()).contains("2026-10-21");
        assertThat(request.model()).isEqualTo("claude-haiku-5-5");
    }

    @Test
    void fallsBackOnUnknownCategoryMissingDateAndCommaAmounts() {
        ExpenseDraft draft = parserReturning("""
                {"description":"Regalo","amount":"12,50","currency":"","category":"GIFTS"}
                """).parse("regalo 12,50");

        assertThat(draft.amount()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(draft.currency()).isEqualTo("EUR");
        assertThat(draft.category()).isEqualTo(Category.OTHER);
        assertThat(draft.date()).isEqualTo(LocalDate.of(2026, 10, 21));
    }

    @Test
    void rejectsTextWithoutAValidAmount() {
        ExpenseParser parser = parserReturning("""
                {"description":"Benzina","amount":0,"currency":"EUR","category":"FUEL","date":"2026-10-21"}
                """);

        assertThatThrownBy(() -> parser.parse("benzina"))
                .isInstanceOf(UnparseableExpenseException.class);
    }

    @Test
    void failsWhenClaudeDoesNotCallTheTool() {
        ExpenseParser parser = new ExpenseParser(
                request -> new MessageResponse("msg_1", List.of(ContentBlock.text("Non so")), "end_turn"),
                PROPS, CLOCK);

        assertThatThrownBy(() -> parser.parse("ciao")).isInstanceOf(LlmException.class);
    }
}
