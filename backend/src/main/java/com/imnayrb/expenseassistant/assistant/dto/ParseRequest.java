package com.imnayrb.expenseassistant.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParseRequest(@NotBlank @Size(max = 500) String text) {
}
