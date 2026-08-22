package com.dro.modules.boss.world.domain;

import com.dro.modules.boss.domain.BossCombatRules;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Regras de ataques contra o Boss Mundial.
 *
 * <p>Cada jogador possui três ataques diários. O dano usa a vida máxima do Boss
 * e uma faixa percentual baseada na chance de vitória: a chance é multiplicada
 * por 0,05 ponto percentual, com dano mínimo de 0,05% da vida máxima.</p>
 */
@UtilityClass
public class WorldBossRules {

    public static final int DAILY_ATTACK_LIMIT = 3;
    public static final int DEFAULT_ATTACK_COOLDOWN_MINUTES = 5;
    public static final double DAMAGE_PERCENT_PER_WIN_CHANCE = 0.05;
    public static final double MIN_DAMAGE_PERCENT = 0.05;

    /** Calcula quantos ataques diários ainda estão disponíveis. */
    public static int dailyAttacksRemaining(long usedToday) {
        long remaining = DAILY_ATTACK_LIMIT - usedToday;
        return (int) Math.max(0, remaining);
    }

    /** Retorna o cooldown efetivo, aplicando cinco minutos quando o cadastro não o configurou. */
    public static int attackCooldownMinutes(int configuredMinutes) {
        return configuredMinutes > 0 ? configuredMinutes : DEFAULT_ATTACK_COOLDOWN_MINUTES;
    }

    /** Verifica se o jogador já consumiu os três ataques do dia. */
    public static boolean dailyLimitReached(long usedToday) {
        return usedToday >= DAILY_ATTACK_LIMIT;
    }

    /**
     * Calcula dano aleatório dentro da faixa determinada pela chance de vitória.
     *
     * @param maxHp vida máxima da instância do Boss
     * @param winChance chance percentual calculada pelo poder do Digimon e do Boss
     * @return dano inteiro mínimo de 1
     */
    public static int calculateDamage(int maxHp, int winChance) {
        double maxPercent = Math.max(MIN_DAMAGE_PERCENT,
                winChance * DAMAGE_PERCENT_PER_WIN_CHANCE);

        double percent = maxPercent <= MIN_DAMAGE_PERCENT
                ? MIN_DAMAGE_PERCENT
                : ThreadLocalRandom.current().nextDouble(MIN_DAMAGE_PERCENT, maxPercent);

        return (int) Math.max(1, Math.round(maxHp * percent / 100.0));
    }

    /** Calcula a experiência concedida pelo golpe conforme o percentual de derrota. */
    public static int hitXp(int baseXpReward, int defeatXpPercent) {
        return (int) Math.max(1, Math.round(baseXpReward * defeatXpPercent / 100.0));
    }

    /** Calcula os Bits concedidos pelo golpe conforme o percentual de derrota. */
    public static int hitBits(int baseBitsReward, int defeatXpPercent) {
        return Math.max(1, (int) Math.round(baseBitsReward * defeatXpPercent / 100.0));
    }

    /** Delega o cálculo da chance de vitória à regra de combate compartilhada. */
    public static int calculateWinChance(double digimonPower, double bossPower) {
        return BossCombatRules.calculateWinChance(digimonPower, bossPower);
    }
}
