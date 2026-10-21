package com.imnayrb.expenseassistant.assistant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * L'intera conversazione, inviata dal client a ogni turno: il backend è stateless, quindi la cronologia la
 * conserva il client. Il primo e l'ultimo messaggio devono essere dell'utente.
 */
public record ChatRequest(@NotEmpty @Size(max = 40) List<@Valid ChatMessage> messages) {

    @AssertTrue(message = "la conversazione deve iniziare e finire con un messaggio dell'utente")
    public boolean isWellFormed() {
        if (messages == null || messages.isEmpty()) {
            return true; // segnalato già da @NotEmpty
        }
        return "user".equals(messages.getFirst().role()) && "user".equals(messages.getLast().role());
    }
}
