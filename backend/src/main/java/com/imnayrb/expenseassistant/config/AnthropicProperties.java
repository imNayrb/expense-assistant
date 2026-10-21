package com.imnayrb.expenseassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings for the Claude API.
 *
 * @param apiKey    API key, read from {@code ANTHROPIC_API_KEY}; when empty the assistant is disabled
 * @param baseUrl   API base URL
 * @param model     model used for parsing and chat
 * @param maxTokens upper bound on the tokens Claude may generate per request
 * @param version   value of the {@code anthropic-version} header
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
