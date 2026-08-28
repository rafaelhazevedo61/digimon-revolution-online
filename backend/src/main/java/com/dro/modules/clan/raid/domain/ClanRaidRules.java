package com.dro.modules.clan.raid.domain;

import com.dro.modules.boss.domain.BossCombatRules;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Clãs.
 */
public final class ClanRaidRules {
    public static final int DEFAULT_ATTACK_COOLDOWN_MINUTES = BossCombatRules.DEFAULT_ATTACK_COOLDOWN_MINUTES;
    public static final double DAMAGE_PERCENT_PER_WIN_CHANCE = BossCombatRules.DAMAGE_PERCENT_PER_WIN_CHANCE;
    public static final double MIN_DAMAGE_PERCENT = BossCombatRules.MIN_DAMAGE_PERCENT;

    public static int attackCooldownMinutes(int configuredMinutes) {
        return BossCombatRules.attackCooldownMinutes(configuredMinutes);
    }

    public static int calculateDamage(int maxHp, int winChance) {
        return BossCombatRules.calculateVariableDamage(maxHp, winChance);
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
