package com.dro.modules.digimon.domain;

/**
 * Regras de capacidade máxima de energia do Digimon.
 */
public final class DigimonEnergyRules {
    public static final int BASE_MAX_ENERGY = 100;
    public static final int MAX_ENERGY_PER_LEVEL = 5;
    public static final long FULL_REGENERATION_DURATION_MILLIS = 8 * 60 * 60 * 1000L + 20 * 60 * 1000L;

    private DigimonEnergyRules() {
    }

    public static int maxEnergyAtLevel(int level) {
        int normalizedLevel = Math.max(1, Math.min(level, DigimonLevelRules.MAX_LEVEL));
        return BASE_MAX_ENERGY + ((normalizedLevel - 1) * MAX_ENERGY_PER_LEVEL);
    }

    public static long regenerationIntervalMillis(int maxEnergy) {
        return FULL_REGENERATION_DURATION_MILLIS / Math.max(1, maxEnergy);
    }

    public static long energyRecovered(long elapsedMillis, int maxEnergy) {
        int normalizedMaxEnergy = Math.max(1, maxEnergy);
        return Math.min(normalizedMaxEnergy,
                elapsedMillis * normalizedMaxEnergy / FULL_REGENERATION_DURATION_MILLIS);
    }
}
