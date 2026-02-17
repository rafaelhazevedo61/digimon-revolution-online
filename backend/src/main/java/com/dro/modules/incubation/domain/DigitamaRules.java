package com.dro.modules.incubation.domain;

import com.dro.modules.inventory.domain.ItemType;

public class DigitamaRules {

    public static boolean isDigitama(ItemType type) {
        return switch (type) {
            case DIGITAMA_FIRE,
                 DIGITAMA_WATER,
                 DIGITAMA_NATURE -> true;
            default -> false;
        };
    }
}
