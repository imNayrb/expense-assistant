package com.imnayrb.expenseassistant.assistant.dto;

import java.util.List;

/** La risposta di Claude, più i tool che ha chiamato (mostrati nell'interfaccia per trasparenza). */
public record ChatResponse(String reply, List<String> toolsUsed) {
}
