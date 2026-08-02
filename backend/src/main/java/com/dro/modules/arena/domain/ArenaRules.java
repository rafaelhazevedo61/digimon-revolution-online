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

    /** Só é possível desafiar oponentes dentro de ±RATING_WINDOW pontos do seu rating. */
    public static final int RATING_WINDOW = 200;

    /** Bits base concedidos ao vencedor (para oponente de rating igual). */
    public static final int WIN_BITS_BASE = 100;

    /** Bits ganhos/perdidos por ponto de diferença de rating (oponente - você). */
    public static final double BITS_PER_RATING_DIFF = 0.25;

    /** Limites da recompensa em bits por vitória. */
    public static final int MIN_WIN_BITS = 25;
    public static final int MAX_WIN_BITS = 200;

    /** True se os ratings estão dentro da janela de desafio permitida. */
    public static boolean withinChallengeWindow(int myRating, int opponentRating) {
        return Math.abs(myRating - opponentRating) <= RATING_WINDOW;
    }

    /**
     * Bits de vitória proporcionais à diferença de rating: bater alguém mais forte
     * (rating maior) rende mais; bater alguém mais fraco rende menos.
     */
    public static int winBits(int myRating, int opponentRating) {
        double raw = WIN_BITS_BASE + (opponentRating - myRating) * BITS_PER_RATING_DIFF;
        int bits = (int) Math.round(raw);
        return Math.max(MIN_WIN_BITS, Math.min(MAX_WIN_BITS, bits));
    }

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
