package com.dro.modules.arena.domain;

public class ArenaRules {

    private ArenaRules() {
    }

    /** Rating inicial de cada Digimon na arena. */
    public static final int INITIAL_RATING = 1000;

    /** Rating mínimo (não fica negativo nem zera). */
    public static final int MIN_RATING = 100;

    /** Fator K do ELO — controla o quanto o rating muda por partida. */
    public static final int K_FACTOR = 32;

    /** Energia consumida pelo atacante por desafio. */
    public static final int ENERGY_COST = 10;

    /** Bits concedidos ao vencedor. */
    public static final int WIN_BITS = 100;

    /** Score esperado do jogador A contra B (probabilidade ELO, 0..1). */
    public static double expectedScore(int ratingA, int ratingB) {
        return 1.0 / (1.0 + Math.pow(10, (ratingB - ratingA) / 400.0));
    }

    /** Novo rating aplicando o resultado (actualScore = 1 vitória, 0 derrota). */
    public static int newRating(int rating, double expected, double actualScore) {
        int updated = (int) Math.round(rating + K_FACTOR * (actualScore - expected));
        return Math.max(MIN_RATING, updated);
    }
}
