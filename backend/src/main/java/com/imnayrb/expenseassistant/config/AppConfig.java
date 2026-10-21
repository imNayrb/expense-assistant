package com.imnayrb.expenseassistant.config;

import com.imnayrb.expenseassistant.assistant.llm.AnthropicLlmClient;
import com.imnayrb.expenseassistant.assistant.llm.LlmClient;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties({AppProperties.class, AnthropicProperties.class})
public class AppConfig {

    /** A single clock in the configured zone, so "today" is consistent and testable. */
    @Bean
    Clock clock(AppProperties properties) {
        return Clock.system(properties.zone());
    }

    @Bean
    LlmClient llmClient(RestClient.Builder builder, AnthropicProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        return new AnthropicLlmClient(builder.requestFactory(requestFactory), properties);
    }

    @Bean
    WebMvcConfigurer corsConfigurer(AppProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(properties.corsAllowedOrigins().toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
