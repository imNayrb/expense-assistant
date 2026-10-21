package com.imnayrb.expenseassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Impostazioni per l'API di Claude.
 *
 * @param apiKey    chiave API, letta da {@code ANTHROPIC_API_KEY}; se vuota l'assistente è disattivato
 * @param baseUrl   URL base dell'API
 * @param model     modello usato per parsing e chat
 * @param maxTokens numero massimo di token che Claude può generare per richiesta
 * @param version   valore dell'header {@code anthropic-version}
 */
@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(
        String apiKey,
        @DefaultValue("https://api.anthropic.com") String baseUrl,
        @DefaultValue("claude-haiku-5-5") String model,
        @DefaultValue("1024") int maxTokens,
        @DefaultValue("2023-06-01") String version) {

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
