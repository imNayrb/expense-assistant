package com.imnayrb.expenseassistant.assistant.llm;

import com.imnayrb.expenseassistant.config.AnthropicProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** {@link LlmClient} backed by the Claude Messages API over HTTP. */
public class AnthropicLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicLlmClient.class);

    private final RestClient restClient;
    private final AnthropicProperties properties;

    public AnthropicLlmClient(RestClient.Builder builder, AnthropicProperties properties) {
        this.properties = properties;
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("anthropic-version", properties.version())
                .build();
    }

    @Override
    public MessageResponse createMessage(MessageRequest request) {
        if (!properties.isConfigured()) {
            throw new AssistantUnavailableException(
                    "The assistant is disabled: set ANTHROPIC_API_KEY to enable it");
        }
        try {
            MessageResponse response = restClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MessageResponse.class);
            if (response == null) {
                throw new LlmException("Empty response from the Claude API");
            }
            return response;
        } catch (RestClientResponseException e) {
            // The body explains the error (invalid model, rate limit...) and never contains the key.
            log.warn("Claude API error {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new LlmException("Claude API returned HTTP " + e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new LlmException("Claude API is unreachable", e);
        }
    }
}
