package com.dro.modules.digimon.domain;

/**
 * Regras de capacidade máxima de energia do Digimon.
 */
public final class DigimonEnergyRules {
    public static final int BASE_MAX_ENERGY = 100;
    public static final int MAX_ENERGY_PER_LEVEL = 5;
    public static final long BASE_REGENERATION_INTERVAL_MILLIS = 5 * 60 * 1000L;

    private DigimonEnergyRules() {
    }

    public static int maxEnergyAtLevel(int level) {
        int normalizedLevel = Math.max(1, Math.min(level, DigimonLevelRules.MAX_LEVEL));
        return BASE_MAX_ENERGY + ((normalizedLevel - 1) * MAX_ENERGY_PER_LEVEL);
    }

    public static long regenerationIntervalMillis(int maxEnergy) {
        return BASE_REGENERATION_INTERVAL_MILLIS * Math.max(1, maxEnergy) / BASE_MAX_ENERGY;
    }
}
