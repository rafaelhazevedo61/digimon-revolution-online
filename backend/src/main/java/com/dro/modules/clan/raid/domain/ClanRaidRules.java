package com.dro.modules.clan.raid.domain;

import com.dro.modules.boss.domain.BossCombatRules;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Clãs.
 */
public final class ClanRaidRules {
    public static final int DAILY_ATTACK_LIMIT = 3;
    public static final int DEFAULT_ATTACK_COOLDOWN_MINUTES = BossCombatRules.DEFAULT_ATTACK_COOLDOWN_MINUTES;
    public static final double DAMAGE_PERCENT_PER_WIN_CHANCE = 0.15;
    public static final int MIN_DAMAGE_PERCENT = 1;

    public static int dailyAttacksRemaining(long usedToday) {
        return dailyAttacksRemaining(usedToday, DAILY_ATTACK_LIMIT);
    }

    public static int dailyAttacksRemaining(long usedToday, int dailyAttackLimit) {
        long remaining = dailyAttackLimit - usedToday;
        return (int) Math.max(0, remaining);
    }

    public static boolean dailyLimitReached(long usedToday) {
        return dailyLimitReached(usedToday, DAILY_ATTACK_LIMIT);
    }

    public static boolean dailyLimitReached(long usedToday, int dailyAttackLimit) {
        return usedToday >= dailyAttackLimit;
    }

    public static int attackCooldownMinutes(int configuredMinutes) {
        return BossCombatRules.attackCooldownMinutes(configuredMinutes);
    }

    public static int calculateDamage(int maxHp, int winChance) {
        int maxPercent = Math.max(MIN_DAMAGE_PERCENT, (int) Math.round(winChance * DAMAGE_PERCENT_PER_WIN_CHANCE));
        int percent = ThreadLocalRandom.current().nextInt(MIN_DAMAGE_PERCENT, maxPercent + 1);
        return (int) Math.max(1, Math.round(maxHp * percent / 100.0));
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
