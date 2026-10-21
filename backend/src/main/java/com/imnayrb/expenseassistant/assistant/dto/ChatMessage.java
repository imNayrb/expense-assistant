package com.imnayrb.expenseassistant.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Un messaggio della cronologia della chat, conservata dal client. */
public record ChatMessage(
        @NotBlank @Pattern(regexp = "user|assistant") String role,
        @NotBlank @Size(max = 2000) String content) {
}
