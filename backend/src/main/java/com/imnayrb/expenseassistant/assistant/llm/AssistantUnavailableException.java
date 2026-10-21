package com.imnayrb.expenseassistant.assistant.llm;

/** The assistant is not configured (no API key). Mapped to 503 Service Unavailable. */
public class AssistantUnavailableException extends RuntimeException {

    public AssistantUnavailableException(String message) {
        super(message);
    }
}
