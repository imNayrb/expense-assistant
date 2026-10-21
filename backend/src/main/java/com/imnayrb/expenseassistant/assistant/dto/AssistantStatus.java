package com.imnayrb.expenseassistant.assistant.dto;

/** Lets the UI know whether the assistant features can be used. */
public record AssistantStatus(boolean enabled, String model) {
}
