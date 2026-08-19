package com.dro.modules.arena.domain;

import com.dro.modules.digimon.domain.enums.Stage;

/**
 * Regras competitivas da Arena, incluindo matchmaking, ELO, recompensas e limites diários.
 *
 * <p>A Arena restringe oponentes por estágio e janela de rating. A chance de
 * vitória usa a força relativa entre atacante e defensor, enquanto o rating
 * utiliza a fórmula ELO com fator {@code K = 32}.</p>
 */
public class ArenaRules {

    private ArenaRules() {
    }

    /** Diferença máxima de stage permitida entre oponentes (0 = só mesmo stage, 1 = adjacente). */
    public static final int STAGE_RANGE = 1;

    /** True se os stages estão dentro do alcance permitido (mesmo stage ou adjacente). */
    public static boolean withinStageRange(Stage a, Stage b) {
        return Math.abs(a.ordinal() - b.ordinal()) <= STAGE_RANGE;
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

    /** Moedas de Arena por combate — vitória rende mais, derrota ainda concede participação. */
    public static final int WIN_ARENA_COINS_MIN = 15;
    public static final int WIN_ARENA_COINS_MAX = 40;
    public static final int LOSS_ARENA_COINS = 5;

    /**
     * Moedas de Arena por vitória: base {@link #WIN_ARENA_COINS_MIN} + bônus por dificuldade
     * (quanto menor a chance de vitória, mais moedas), limitado a {@link #WIN_ARENA_COINS_MAX}.
     */
    public static int winArenaCoins(int winChance) {
        int coins = WIN_ARENA_COINS_MIN + (100 - winChance) / 5;
        return Math.min(WIN_ARENA_COINS_MAX, Math.max(WIN_ARENA_COINS_MIN, coins));
    }

    /** Moedas de Arena por derrota (participação). */
    public static int lossArenaCoins() {
        return LOSS_ARENA_COINS;
    }

    /** Faixa da chance de vitória na arena (evita 0%/100% garantidos). */
    public static final int MIN_WIN_CHANCE = 5;
    public static final int MAX_WIN_CHANCE = 95;

    /**
     * Chance de vitória do atacante (%), proporcional à força relativa:
     * {@code atk / (atk + def)}. Poderes iguais → ~50% (empate justo),
     * ao contrário da fórmula de boss em que poderes iguais dariam ~95%.
     * Limitada a [MIN_WIN_CHANCE, MAX_WIN_CHANCE].
     */
    public static int winChance(double attackerPower, double defenderPower) {
        double total = attackerPower + defenderPower;
        if (total <= 0) return 50;
        int chance = (int) Math.round((attackerPower / total) * 100);
        return Math.min(MAX_WIN_CHANCE, Math.max(MIN_WIN_CHANCE, chance));
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

    /** Máximo de desafios que um jogador pode fazer por dia (reset na meia-noite do fuso do servidor). */
    public static final int DAILY_CHALLENGE_LIMIT = 5;

    /** Intervalo mínimo (minutos) antes de poder desafiar o mesmo alvo novamente. */
    public static final int TARGET_COOLDOWN_MINUTES = 30;

    /** Desafios restantes no dia, dado quantos já foram usados (nunca negativo). */
    public static int remainingDailyChallenges(long usedToday) {
        long remaining = DAILY_CHALLENGE_LIMIT - usedToday;
        return (int) Math.max(0, remaining);
    }

    /** True se o limite diário de desafios já foi atingido. */
    public static boolean dailyLimitReached(long usedToday) {
        return usedToday >= DAILY_CHALLENGE_LIMIT;
    }

    /** Tier de Arena correspondente a um rating. */
    public static ArenaTier tierFor(int rating) {
        return ArenaTier.fromRating(rating);
    }

    /**
     * Pontos que faltam para o próximo tier (0 se já está no tier máximo).
     */
    public static int pointsToNextTier(int rating) {
        ArenaTier tier = ArenaTier.fromRating(rating);
        ArenaTier next = tier.next();
        return next == null ? 0 : Math.max(0, next.getMinRating() - rating);
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
