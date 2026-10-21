package com.imnayrb.expenseassistant.config;

import java.time.ZoneId;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Application-level settings.
 *
 * @param zone                time zone used to resolve "today" (and relative dates like "ieri")
 * @param corsAllowedOrigins  origins allowed to call the API from a browser
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue("Europe/Rome") ZoneId zone,
        @DefaultValue("http://localhost:4200") List<String> corsAllowedOrigins) {
}
