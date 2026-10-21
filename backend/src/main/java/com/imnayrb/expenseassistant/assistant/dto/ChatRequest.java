package com.imnayrb.expenseassistant.assistant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * The whole conversation, sent by the client on every turn: the backend is stateless, so the client owns the
 * history. The last message must come from the user.
 */
public record ChatRequest(@NotEmpty @Size(max = 40) List<@Valid ChatMessage> messages) {

    @AssertTrue(message = "the conversation must start and end with a user message")
    public boolean isWellFormed() {
        if (messages == null || messages.isEmpty()) {
            return true; // reported by @NotEmpty
        }
        return "user".equals(messages.getFirst().role()) && "user".equals(messages.getLast().role());
    }
}
