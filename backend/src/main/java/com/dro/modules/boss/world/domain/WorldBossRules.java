package com.dro.modules.boss.world.domain;

import com.dro.modules.boss.domain.BossCombatRules;

/**
 * Regras de ataques contra o Boss Mundial.
 *
 * <p>O dano usa a vida máxima do Boss e uma faixa percentual baseada na chance
 * de vitória: a chance é multiplicada por 0,05 ponto percentual, com dano
 * mínimo de 0,05% da vida máxima.</p>
 */
public final class WorldBossRules {
    public static final int DEFAULT_ATTACK_COOLDOWN_MINUTES = BossCombatRules.DEFAULT_ATTACK_COOLDOWN_MINUTES;
    public static final double DAMAGE_PERCENT_PER_WIN_CHANCE = BossCombatRules.DAMAGE_PERCENT_PER_WIN_CHANCE;
    public static final double MIN_DAMAGE_PERCENT = BossCombatRules.MIN_DAMAGE_PERCENT;

    /**
     * Retorna o cooldown efetivo, aplicando cinco minutos quando o cadastro não o configurou.
     */
    public static int attackCooldownMinutes(int configuredMinutes) {
        return BossCombatRules.attackCooldownMinutes(configuredMinutes);
    }

    /**
     * Calcula dano aleatório dentro da faixa determinada pela chance de vitória.
     *
     * @param maxHp vida máxima da instância do Boss
     * @param winChance chance percentual calculada pelo poder do Digimon e do Boss
     * @return dano inteiro mínimo de 1
     */
    public static int calculateDamage(int maxHp, int winChance) {
        return BossCombatRules.calculateVariableDamage(maxHp, winChance);
    }

    /**
     * Calcula a experiência concedida pelo golpe conforme o percentual de derrota.
     */
    public static int hitXp(int baseXpReward, int defeatXpPercent) {
        return (int) Math.max(1, Math.round(baseXpReward * defeatXpPercent / 100.0));
    }

    /**
     * Calcula os Bits concedidos pelo golpe conforme o percentual de derrota.
     */
    public static int hitBits(int baseBitsReward, int defeatXpPercent) {
        return Math.max(1, (int) Math.round(baseBitsReward * defeatXpPercent / 100.0));
    }

    /**
     * Delega o cálculo da chance de vitória à regra de combate compartilhada.
     */
    public static int calculateWinChance(double digimonPower, double bossPower) {
        return BossCombatRules.calculateWinChance(digimonPower, bossPower);
    }

    private WorldBossRules() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
