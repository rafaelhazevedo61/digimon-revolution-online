package com.dro.modules.incubation.domain;

import com.dro.modules.inventory.domain.ItemType;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Incubação.
 */
public class DigitamaRules {

    public static boolean isDigitama(ItemType type) {
        return switch (type) {
            case DIGITAMA_FIRE,
                 DIGITAMA_WATER,
                 DIGITAMA_NATURE,
                 DIGITAMA_STARTER -> true;
            default -> false;
        };
    }
}
