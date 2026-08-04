package com.dro.modules.arena.domain;

/**
 * Faixas (tiers) de Arena derivadas do rating. Sem estado no banco — calculadas
 * a partir do {@code arenaRating} atual do Digimon.
 */
public enum ArenaTier {

    BRONZE("Bronze", 0, 1000),
    PRATA("Prata", 1000, 1200),
    OURO("Ouro", 1200, 1500),
    PLATINA("Platina", 1500, 1900),
    DIAMANTE("Diamante", 1900, Integer.MAX_VALUE);

    private final String label;
    private final int minRating;
    private final int maxRatingExclusive;

    ArenaTier(String label, int minRating, int maxRatingExclusive) {
        this.label = label;
        this.minRating = minRating;
        this.maxRatingExclusive = maxRatingExclusive;
    }

    public String getLabel() {
        return label;
    }

    public int getMinRating() {
        return minRating;
    }

    public int getMaxRatingExclusive() {
        return maxRatingExclusive;
    }

    /** Tier correspondente a um rating. */
    public static ArenaTier fromRating(int rating) {
        ArenaTier result = BRONZE;
        for (ArenaTier tier : values()) {
            if (rating >= tier.minRating) {
                result = tier;
            }
        }
        return result;
    }

    /** Próximo tier acima, ou {@code null} se já está no tier máximo. */
    public ArenaTier next() {
        int ordinal = ordinal();
        return ordinal < values().length - 1 ? values()[ordinal + 1] : null;
    }
}
