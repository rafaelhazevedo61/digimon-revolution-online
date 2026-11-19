package com.dro.modules.clan.raid.domain;

import com.dro.modules.boss.domain.BossCombatRules;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Clãs.
 */
public final class ClanRaidRules {
    public static final int DEFAULT_ATTACK_COOLDOWN_MINUTES = BossCombatRules.DEFAULT_ATTACK_COOLDOWN_MINUTES;
    /** Percentual mínimo de dano por ataque de Clan Raid: 0,75% da vida máxima. */
    public static final double MIN_DAMAGE_PERCENT = 0.75;
    /** Crescimento do teto da faixa por ponto percentual de chance de vitória. */
    public static final double DAMAGE_PERCENT_PER_WIN_CHANCE = 0.07;

    public static int attackCooldownMinutes(int configuredMinutes) {
        return BossCombatRules.attackCooldownMinutes(configuredMinutes);
    }

    /**
     * Calcula dano variável específico para Clan Raid.
     *
     * <p>A faixa começa em 0,75% da vida máxima e o teto cresce 0,07 ponto
     * percentual por ponto de chance de vitória. Com 5% de chance, um raid de
     * 50.000 HP recebe entre 375 e 550 de dano; com 95%, entre 375 e 3.700.
     * A aleatoriedade preserva a variação sem repetir o balanceamento mais
     * punitivo do Boss Mundial.</p>
     */
    public static int calculateDamage(int maxHp, int winChance) {
        if (maxHp <= 0) return 1;
        double maxPercent = Math.max(MIN_DAMAGE_PERCENT,
                MIN_DAMAGE_PERCENT + Math.max(0, winChance) * DAMAGE_PERCENT_PER_WIN_CHANCE);
        double percent = maxPercent <= MIN_DAMAGE_PERCENT
                ? MIN_DAMAGE_PERCENT
                : ThreadLocalRandom.current().nextDouble(MIN_DAMAGE_PERCENT, maxPercent);
        return Math.max(1, (int) Math.round(maxHp * percent / 100.0));
    }

    public static int hitXp(int baseXpReward, int defeatXpPercent) {
        return (int) Math.max(1, Math.round(baseXpReward * defeatXpPercent / 100.0));
    }

    public static int hitBits(int baseBitsReward, int defeatXpPercent) {
        return Math.max(1, (int) Math.round(baseBitsReward * defeatXpPercent / 100.0));
    }

    public static int calculateWinChance(double digimonPower, double bossPower) {
        return BossCombatRules.calculateWinChance(digimonPower, bossPower);
    }

    private ClanRaidRules() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
