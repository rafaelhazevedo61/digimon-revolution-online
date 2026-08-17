package com.dro.modules.boss.world.domain;

import com.dro.modules.boss.domain.BossCombatRules;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class WorldBossRules {

    public static final int DAILY_ATTACK_LIMIT = 3;
    public static final double DAMAGE_PERCENT_PER_WIN_CHANCE = 0.05;
    public static final double MIN_DAMAGE_PERCENT = 0.05;

    public static int dailyAttacksRemaining(long usedToday) {
        long remaining = DAILY_ATTACK_LIMIT - usedToday;
        return (int) Math.max(0, remaining);
    }

    public static boolean dailyLimitReached(long usedToday) {
        return usedToday >= DAILY_ATTACK_LIMIT;
    }

    public static int calculateDamage(int maxHp, int winChance) {
        double maxPercent = Math.max(MIN_DAMAGE_PERCENT,
                winChance * DAMAGE_PERCENT_PER_WIN_CHANCE);

        double percent = maxPercent <= MIN_DAMAGE_PERCENT
                ? MIN_DAMAGE_PERCENT
                : ThreadLocalRandom.current().nextDouble(MIN_DAMAGE_PERCENT, maxPercent);

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
}
