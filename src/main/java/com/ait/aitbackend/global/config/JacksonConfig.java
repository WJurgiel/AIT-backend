package com.ait.aitbackend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracja Jacksona (serializacja/deserializacja JSON).
 * Rejestruje dodatkowe moduły (np. Java Time Module),
 * aby poprawnie obsługiwać typy takie jak Instant, LocalDate itd.
 */
@Configuration
public class JacksonConfig {

    /**
     * Globalny ObjectMapper używany w całej aplikacji.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules();
    }
}