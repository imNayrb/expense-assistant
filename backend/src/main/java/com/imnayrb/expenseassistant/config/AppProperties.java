package com.imnayrb.expenseassistant.config;

import java.time.ZoneId;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Impostazioni generali dell'applicazione.
 *
 * @param zone               fuso orario usato per calcolare "oggi" (e le date relative come "ieri")
 * @param corsAllowedOrigins origini autorizzate a chiamare l'API da un browser
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue("Europe/Rome") ZoneId zone,
        @DefaultValue("http://localhost:4200") List<String> corsAllowedOrigins) {
}
