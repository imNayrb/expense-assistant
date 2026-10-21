package com.imnayrb.expenseassistant.assistant.dto;

import java.util.List;

/** Claude's answer, plus the tools it called (shown in the UI for transparency). */
public record ChatResponse(String reply, List<String> toolsUsed) {
}
