package com.imnayrb.expenseassistant.assistant.llm;

/** L'assistente non è configurato (manca la chiave API). Diventa 503 Service Unavailable. */
public class AssistantUnavailableException extends RuntimeException {

    public AssistantUnavailableException(String message) {
        super(message);
    }
}
