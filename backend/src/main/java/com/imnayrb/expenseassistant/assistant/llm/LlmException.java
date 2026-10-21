package com.imnayrb.expenseassistant.assistant.llm;

/** L'API di Claude ha fallito o ha risposto con qualcosa di inutilizzabile. Diventa 502 Bad Gateway. */
public class LlmException extends RuntimeException {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
