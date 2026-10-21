package com.imnayrb.expenseassistant.assistant.llm;

import com.imnayrb.expenseassistant.config.AnthropicProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Implementazione di {@link LlmClient} che chiama la Messages API di Claude via HTTP. */
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
                    "L'assistente è disattivato: imposta ANTHROPIC_API_KEY per abilitarlo");
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
                throw new LlmException("Risposta vuota dall'API di Claude");
            }
            return response;
        } catch (RestClientResponseException e) {
            // Il corpo spiega l'errore (modello non valido, rate limit...) e non contiene mai la chiave.
            log.warn("Errore dall'API di Claude {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new LlmException("L'API di Claude ha risposto con HTTP " + e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new LlmException("L'API di Claude non è raggiungibile", e);
        }
    }
}
