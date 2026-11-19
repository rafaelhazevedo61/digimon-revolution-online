package com.dro.modules.clan.raid.domain;

/**
 * Tipos de Baú concedidos durante o ciclo de uma incursão de clã.
 */
public enum ClanRaidRewardType {
    /** Baú concedido por cada ataque válido realizado pelo jogador. */
    ATTEMPT("ATTEMPT"),
    /** Baú concedido ao jogador com maior dano acumulado na derrota. */
    TOP_DAMAGE("TOP_DAMAGE"),
    /** Baú concedido ao jogador que reduziu o HP do chefe a zero. */
    FINAL_BLOW("FINAL_BLOW");

    private final String code;

    ClanRaidRewardType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
