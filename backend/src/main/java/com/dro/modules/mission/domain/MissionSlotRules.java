package com.dro.modules.mission.domain;

/**
 * Regras de capacidade dos slots paralelos de missão.
 */
public final class MissionSlotRules {
    public static final int TOTAL_SLOTS = 3;
    public static final int MIN_UNLOCKED_SLOTS = 1;

    private MissionSlotRules() {
    }

    public static int normalizeUnlockedSlots(int unlockedSlots) {
        return Math.max(MIN_UNLOCKED_SLOTS, Math.min(TOTAL_SLOTS, unlockedSlots));
    }

    public static boolean isUnlocked(int slotNumber, int unlockedSlots) {
        return slotNumber >= 1 && slotNumber <= normalizeUnlockedSlots(unlockedSlots);
    }
}
