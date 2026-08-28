package com.dro.modules.boss.domain;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Fórmulas de poder e chance de vitória usadas no combate contra Boss.
 *
 * <p>O poder é calculado por {@code HP * 0,30 + ATK * 1,50 + DEF}. A chance
 * bruta é a razão entre o poder do Digimon e o poder do Boss, limitada entre
 * 5% e 95%. O limiar de 30% identifica combates de risco elevado.</p>
 */
public class BossCombatRules {

    private static final double HP_WEIGHT = 0.30;
    private static final double ATK_WEIGHT = 1.50;
    private static final double DEF_WEIGHT = 1.00;
    private static final int MIN_CHANCE = 5;
    private static final int MAX_CHANCE = 95;
    public static final int MIN_THRESHOLD = 30;
    public static final int DEFAULT_ATTACK_COOLDOWN_MINUTES = 5;
    public static final double DAMAGE_PERCENT_PER_WIN_CHANCE = 0.05;
    public static final double MIN_DAMAGE_PERCENT = 0.05;

    /** Calcula o poder ponderado de um combatente. */
    public static double calculatePower(int hp, int atk, int def) {
        return hp * HP_WEIGHT + atk * ATK_WEIGHT + def * DEF_WEIGHT;
    }

    /** Calcula e limita a chance percentual de vitória do Digimon. */
    public static int calculateWinChance(double digimonPower, double bossPower) {
        if (bossPower <= 0) return MAX_CHANCE;
        int chance = (int) Math.round((digimonPower / bossPower) * 100);
        return Math.min(MAX_CHANCE, Math.max(MIN_CHANCE, chance));
    }

    /**
     * Calcula dano aleatório dentro da faixa percentual determinada pela chance de vitória.
     * A faixa começa em 0,05% da vida máxima e cresce 0,05 ponto percentual
     * para cada ponto de chance de vitória.
     */
    public static int calculateVariableDamage(int maxHp, int winChance) {
        double maxPercent = Math.max(MIN_DAMAGE_PERCENT, winChance * DAMAGE_PERCENT_PER_WIN_CHANCE);
        double percent = maxPercent <= MIN_DAMAGE_PERCENT
                ? MIN_DAMAGE_PERCENT
                : ThreadLocalRandom.current().nextDouble(MIN_DAMAGE_PERCENT, maxPercent);
        return (int) Math.max(1, Math.round(maxHp * percent / 100.0));
    }

    /** Retorna o cooldown efetivo, usando cinco minutos quando não há valor configurado. */
    public static int attackCooldownMinutes(int configuredMinutes) {
        return configuredMinutes > 0 ? configuredMinutes : DEFAULT_ATTACK_COOLDOWN_MINUTES;
    }

    /** Verifica se a chance está abaixo do limiar mínimo recomendado. */
    public static boolean isBelowThreshold(int winChance) {
        return winChance < MIN_THRESHOLD;
    }
}
