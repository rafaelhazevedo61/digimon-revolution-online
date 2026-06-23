package com.dro.modules.boss.domain;

public class BossCombatRules {

    private static final double HP_WEIGHT = 0.30;
    private static final double ATK_WEIGHT = 1.50;
    private static final double DEF_WEIGHT = 1.00;
    private static final int MIN_CHANCE = 5;
    private static final int MAX_CHANCE = 95;

    public static double calculatePower(int hp, int atk, int def) {
        return hp * HP_WEIGHT + atk * ATK_WEIGHT + def * DEF_WEIGHT;
    }

    public static int calculateWinChance(double digimonPower, double bossPower) {
        if (bossPower <= 0) return MAX_CHANCE;
        int chance = (int) Math.round((digimonPower / bossPower) * 100);
        return Math.min(MAX_CHANCE, Math.max(MIN_CHANCE, chance));
    }
}
