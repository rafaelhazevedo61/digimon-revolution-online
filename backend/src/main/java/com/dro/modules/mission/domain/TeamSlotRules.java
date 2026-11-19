package com.dro.modules.mission.domain;

/** Regras de capacidade dos times salvos para missões. */
public final class TeamSlotRules {
    public static final int DEFAULT_SLOTS = 3;
    public static final int MAX_SLOTS = 10;

    private TeamSlotRules() {
    }

    public static int normalizeSlots(int slots) {
        return Math.max(DEFAULT_SLOTS, Math.min(MAX_SLOTS, slots));
    }

    public static boolean canCreate(int currentTeamCount, int unlockedSlots) {
        return currentTeamCount < Math.max(DEFAULT_SLOTS, Math.min(MAX_SLOTS, unlockedSlots));
    }
}
