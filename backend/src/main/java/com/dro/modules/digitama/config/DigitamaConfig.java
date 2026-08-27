package com.dro.modules.digitama.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Configura quais Digimons BABY podem participar dos pools de Digitama.
 *
 * <p>Esta é uma solução temporária de configuração. A intenção é migrar a
 * origem desta elegibilidade para {@code available_contents} no futuro.</p>
 */
@Component
@ConfigurationProperties(prefix = "dro.digitama")
public class DigitamaConfig {
    private Map<String, Boolean> babyDigimons = new LinkedHashMap<>();

    public Map<String, Boolean> getBabyDigimons() {
        return babyDigimons;
    }

    public void setBabyDigimons(Map<String, Boolean> babyDigimons) {
        this.babyDigimons = new LinkedHashMap<>();
        if (babyDigimons != null) {
            babyDigimons.forEach((name, enabled) -> this.babyDigimons.put(normalize(name), enabled));
        }
    }

    public boolean isBabyEnabled(String digimonName) {
        return digimonName != null && Boolean.TRUE.equals(babyDigimons.get(normalize(digimonName)));
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
