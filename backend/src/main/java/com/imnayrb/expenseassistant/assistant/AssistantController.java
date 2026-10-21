package com.imnayrb.expenseassistant.assistant;

import com.imnayrb.expenseassistant.assistant.dto.AssistantStatus;
import com.imnayrb.expenseassistant.assistant.dto.ChatRequest;
import com.imnayrb.expenseassistant.assistant.dto.ChatResponse;
import com.imnayrb.expenseassistant.assistant.dto.ParseRequest;
import com.imnayrb.expenseassistant.config.AnthropicProperties;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final ExpenseParser parser;
    private final ChatAssistant chat;
    private final AnthropicProperties properties;

    public AssistantController(ExpenseParser parser, ChatAssistant chat, AnthropicProperties properties) {
        this.parser = parser;
        this.chat = chat;
        this.properties = properties;
    }

    @GetMapping("/status")
    public AssistantStatus status() {
        return new AssistantStatus(properties.isConfigured(), properties.model());
    }

    /** Dal linguaggio naturale a una bozza di spesa. Non viene salvato nulla. */
    @PostMapping("/parse")
    public ExpenseDraft parse(@Valid @RequestBody ParseRequest request) {
        return parser.parse(request.text());
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chat.reply(request.messages());
    }
}
