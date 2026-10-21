package com.imnayrb.expenseassistant.assistant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.imnayrb.expenseassistant.assistant.dto.ChatMessage;
import com.imnayrb.expenseassistant.assistant.dto.ChatResponse;
import com.imnayrb.expenseassistant.assistant.llm.ContentBlock;
import com.imnayrb.expenseassistant.assistant.llm.LlmClient;
import com.imnayrb.expenseassistant.assistant.llm.LlmException;
import com.imnayrb.expenseassistant.assistant.llm.Message;
import com.imnayrb.expenseassistant.assistant.llm.MessageRequest;
import com.imnayrb.expenseassistant.assistant.llm.MessageResponse;
import com.imnayrb.expenseassistant.config.AnthropicProperties;
import com.imnayrb.expenseassistant.expense.Category;
import com.imnayrb.expenseassistant.expense.ExpenseService;
import com.imnayrb.expenseassistant.expense.dto.CategoryTotal;
import com.imnayrb.expenseassistant.expense.dto.ExpenseSummary;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChatAssistantTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules().build();
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");
    private static final Clock CLOCK = Clock.fixed(ZonedDateTime.of(2026, 10, 21, 10, 0, 0, 0, ROME).toInstant(), ROME);
    private static final AnthropicProperties PROPS =
            new AnthropicProperties("test-key", "https://api.anthropic.com", "claude-haiku-5-5", 1024, "2023-06-01");

    private final ExpenseService expenseService = mock(ExpenseService.class);
    private final List<MessageRequest> requests = new ArrayList<>();

    /** A fake Claude that replays scripted responses and records every request. */
    private ChatAssistant assistantReplying(MessageResponse... responses) {
        Deque<MessageResponse> queue = new ArrayDeque<>(List.of(responses));
        LlmClient fake = request -> {
            // Copy: the assistant keeps mutating its message list after the call.
            requests.add(new MessageRequest(request.model(), request.maxTokens(), request.system(),
                    List.copyOf(request.messages()), request.tools(), request.toolChoice()));
            return queue.isEmpty()
                    ? new MessageResponse("loop", List.of(toolUse("get_summary", "{}")), "tool_use")
                    : queue.poll();
        };
        return new ChatAssistant(fake, new ExpenseTools(expenseService, MAPPER), PROPS, CLOCK);
    }

    @Test
    void runsToolsUntilClaudeAnswers() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        when(expenseService.summary(from, to)).thenReturn(new ExpenseSummary(from, to, new BigDecimal("42.50"), 2,
                List.of(new CategoryTotal(Category.RESTAURANTS, new BigDecimal("42.50"), 2L))));

        ChatAssistant assistant = assistantReplying(
                new MessageResponse("msg_1",
                        List.of(ContentBlock.text("Controllo..."),
                                toolUse("get_summary", "{\"from\":\"2026-10-01\",\"to\":\"2026-10-31\"}")),
                        "tool_use"),
                new MessageResponse("msg_2", List.of(ContentBlock.text("A ottobre hai speso 42,50 €.")), "end_turn"));

        ChatResponse response = assistant.reply(List.of(new ChatMessage("user", "Quanto ho speso a ottobre?")));

        assertThat(response.reply()).isEqualTo("A ottobre hai speso 42,50 €.");
        assertThat(response.toolsUsed()).containsExactly("get_summary");
        verify(expenseService).summary(from, to);

        // Second request = user question, Claude's tool_use turn, our tool_result.
        List<Message> sent = requests.get(1).messages();
        assertThat(sent).extracting(Message::role).containsExactly("user", "assistant", "user");
        ContentBlock result = sent.get(2).content().getFirst();
        assertThat(result.type()).isEqualTo(ContentBlock.TOOL_RESULT);
        assertThat(result.toolUseId()).isEqualTo("toolu_get_summary");
        assertThat(result.isError()).isNull();
        assertThat(MAPPER.readTree(result.content()).get("total").decimalValue())
                .isEqualByComparingTo(new BigDecimal("42.50"));
    }

    @Test
    void reportsToolErrorsToClaudeInsteadOfFailing() {
        ChatAssistant assistant = assistantReplying(
                new MessageResponse("msg_1", List.of(toolUse("get_summary", "{\"from\":\"not-a-date\"}")), "tool_use"),
                new MessageResponse("msg_2", List.of(ContentBlock.text("Non ho capito il periodo.")), "end_turn"));

        ChatResponse response = assistant.reply(List.of(new ChatMessage("user", "Quanto ho speso?")));

        assertThat(response.reply()).isEqualTo("Non ho capito il periodo.");
        ContentBlock result = requests.get(1).messages().get(2).content().getFirst();
        assertThat(result.isError()).isTrue();
    }

    @Test
    void stopsAfterTooManyToolRounds() {
        ChatAssistant assistant = assistantReplying();

        assertThatThrownBy(() -> assistant.reply(List.of(new ChatMessage("user", "Quanto ho speso?"))))
                .isInstanceOf(LlmException.class);
        assertThat(requests).hasSize(ChatAssistant.MAX_TOOL_ROUNDS + 1);
    }

    private static ContentBlock toolUse(String name, String inputJson) {
        try {
            return ContentBlock.toolUse("toolu_" + name, name, MAPPER.readTree(inputJson));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
