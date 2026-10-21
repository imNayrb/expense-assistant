package com.imnayrb.expenseassistant.assistant.llm;

/** The Claude API failed or answered with something we can't use. Mapped to 502 Bad Gateway. */
public class LlmException extends RuntimeException {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
