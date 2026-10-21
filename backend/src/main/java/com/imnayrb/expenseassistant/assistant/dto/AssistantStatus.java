package com.imnayrb.expenseassistant.assistant.dto;

/** Dice all'interfaccia se le funzioni dell'assistente sono utilizzabili. */
public record AssistantStatus(boolean enabled, String model) {
}
