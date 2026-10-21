package com.imnayrb.expenseassistant.assistant;

import com.imnayrb.expenseassistant.assistant.dto.ChatMessage;
import com.imnayrb.expenseassistant.assistant.dto.ChatResponse;
import com.imnayrb.expenseassistant.assistant.llm.ContentBlock;
import com.imnayrb.expenseassistant.assistant.llm.LlmClient;
import com.imnayrb.expenseassistant.assistant.llm.LlmException;
import com.imnayrb.expenseassistant.assistant.llm.Message;
import com.imnayrb.expenseassistant.assistant.llm.MessageRequest;
import com.imnayrb.expenseassistant.assistant.llm.MessageResponse;
import com.imnayrb.expenseassistant.assistant.llm.ToolChoice;
import com.imnayrb.expenseassistant.config.AnthropicProperties;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Risponde alle domande sulle spese con un loop agentico:
 * <ol>
 *   <li>invia a Claude la conversazione e le definizioni dei tool;</li>
 *   <li>se Claude si ferma con {@code tool_use}, esegue i tool richiesti e gli rimanda i risultati;</li>
 *   <li>ripete finché Claude risponde con del testo, fino a {@link #MAX_TOOL_ROUNDS} giri.</li>
 * </ol>
 * Il limite di giri protegge da loop infiniti e da costi fuori controllo.
 */
@Service
public class ChatAssistant {

    static final int MAX_TOOL_ROUNDS = 5;

    private final LlmClient llm;
    private final ExpenseTools tools;
    private final AnthropicProperties properties;
    private final Clock clock;

    public ChatAssistant(LlmClient llm, ExpenseTools tools, AnthropicProperties properties, Clock clock) {
        this.llm = llm;
        this.tools = tools;
        this.properties = properties;
        this.clock = clock;
    }

    public ChatResponse reply(List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();
        for (ChatMessage message : history) {
            messages.add(new Message(message.role(), List.of(ContentBlock.text(message.content()))));
        }
        String system = systemPrompt(LocalDate.now(clock));
        List<String> toolsUsed = new ArrayList<>();

        for (int round = 0; round <= MAX_TOOL_ROUNDS; round++) {
            MessageResponse response = llm.createMessage(new MessageRequest(
                    properties.model(), properties.maxTokens(), system, messages, tools.definitions(),
                    ToolChoice.auto()));

            if (!response.wantsToolUse()) {
                return new ChatResponse(response.text(), toolsUsed);
            }

            // Il turno di Claude (con i suoi blocchi tool_use) va rimandato indietro prima dei risultati.
            messages.add(Message.assistant(response.content()));
            List<ContentBlock> results = new ArrayList<>();
            for (ContentBlock toolUse : response.toolUses()) {
                toolsUsed.add(toolUse.name());
                results.add(tools.execute(toolUse));
            }
            messages.add(Message.user(results));
        }
        throw new LlmException("L'assistente ha avuto bisogno di troppi passaggi per rispondere");
    }

    /** Prompt in inglese, ma con risposte richieste in italiano. */
    static String systemPrompt(LocalDate today) {
        return """
                You are the assistant of a personal expense tracker. Answer in Italian, briefly and clearly.
                Today is %s (%s).

                - Use the tools to read real data: never invent amounts or expenses.
                - Turn periods like "questo mese", "ottobre", "la settimana scorsa" into explicit date ranges.
                - Format amounts like "42,50 €".
                - Save an expense only if the user explicitly asks you to record it.
                - If a question is not about expenses, say politely that you can only help with expenses.
                """.formatted(today, today.getDayOfWeek());
    }
}
