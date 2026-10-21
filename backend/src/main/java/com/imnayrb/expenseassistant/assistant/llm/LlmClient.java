package com.imnayrb.expenseassistant.assistant.llm;

/**
 * Port to the language model. Everything that talks to Claude goes through this interface, so the rest of
 * the code never depends on HTTP details and tests can plug in a fake.
 */
@FunctionalInterface
public interface LlmClient {

    /**
     * Sends one request to the Messages API.
     *
     * @throws AssistantUnavailableException if no API key is configured
     * @throws LlmException                  if the API call fails
     */
    MessageResponse createMessage(MessageRequest request);
}
