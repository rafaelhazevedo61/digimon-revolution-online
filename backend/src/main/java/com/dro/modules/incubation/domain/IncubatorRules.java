package com.dro.modules.incubation.domain;

import com.dro.modules.inventory.domain.ItemType;

import java.time.Duration;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Incubação.
 */
public class IncubatorRules {

    public static Duration getIncubationTime(ItemType incubatorType) {

        return switch (incubatorType) {
            case INCUBATOR_COMMON -> Duration.ofMinutes(5);
            case INCUBATOR_RARE -> Duration.ofMinutes(2);
            case INCUBATOR_EPIC -> Duration.ofSeconds(30);
            default -> throw new IllegalArgumentException("Invalid incubator type");
        };
    }
}
