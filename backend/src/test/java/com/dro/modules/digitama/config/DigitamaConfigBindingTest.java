package com.dro.modules.digitama.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DigitamaConfigBindingTest {

    @Test
    void bindsBabyNamesContainingSpacesFromApplicationYaml() throws IOException {
        PropertySource<?> source = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yml"))
                .get(0);
        DigitamaConfig config = new Binder(ConfigurationPropertySources.from(source))
                .bind("dro.digitama", Bindable.of(DigitamaConfig.class))
                .orElseThrow(() -> new AssertionError("dro.digitama could not be bound"));

        assertTrue(config.isBabyEnabled("Yukimi Botamon"));
    }
}
