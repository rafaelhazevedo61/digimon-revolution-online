package com.dro.modules.boss.world.domain;

/**
 * Tipos de Baú que podem ser concedidos durante o ciclo diário do Boss Mundial.
 */
public enum WorldBossRewardType {
    /** Baú concedido por cada ataque válido realizado pelo jogador. */
    ATTEMPT("ATTEMPT"),
    /** Baú concedido ao jogador com maior dano acumulado na derrota. */
    TOP_DAMAGE("TOP_DAMAGE"),
    /** Baú concedido ao jogador que reduziu o HP do Boss a zero. */
    FINAL_BLOW("FINAL_BLOW");

    private final String code;

    WorldBossRewardType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
