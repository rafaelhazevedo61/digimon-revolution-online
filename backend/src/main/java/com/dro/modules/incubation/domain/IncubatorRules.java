package com.dro.modules.incubation.domain;

import com.dro.modules.inventory.domain.ItemType;

import java.time.Duration;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Incubação.
 */
public final class IncubatorRules {
    public static final int TOTAL_SLOTS = 3;
    public static final int DEFAULT_UNLOCKED_SLOTS = 1;

    private IncubatorRules() {
    }

    public static Duration getIncubationTime(ItemType incubatorType) {
        return switch (incubatorType) {
            case INCUBATOR_COMMON -> Duration.ofMinutes(5);
            case INCUBATOR_RARE -> Duration.ofMinutes(2);
            case INCUBATOR_EPIC -> Duration.ofSeconds(30);
            case INCUBATOR_LEGENDARY -> Duration.ofSeconds(1);
            default -> throw new IllegalArgumentException("Invalid incubator type");
        };
    }

    public static boolean isValidSlot(int slotNumber) {
        return slotNumber >= 1 && slotNumber <= TOTAL_SLOTS;
    }

    public static boolean isUnlocked(int slotNumber, int unlockedSlots) {
        return isValidSlot(slotNumber) && slotNumber <= unlockedSlots;
    }
}
