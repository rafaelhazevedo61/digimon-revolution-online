package com.dro.modules.mission.domain;

/**
 * Regras de progressão das recompensas de uma mesma missão.
 */
public final class MissionProgressionRules {
    public static final int MAX_REWARD_PROGRESS_COMPLETIONS = 100;
    public static final double PROGRESS_INCREMENT = 0.01;

    private MissionProgressionRules() {
    }

    /**
     * Retorna o multiplicador aplicado à recompensa antes dos bônus externos.
     * O bônus progressivo é limitado a 100 conclusões anteriores (2,00x).
     */
    public static double rewardMultiplier(int previousCompletions) {
        int boundedCompletions = Math.max(0,
                Math.min(previousCompletions, MAX_REWARD_PROGRESS_COMPLETIONS));
        return 1.0 + boundedCompletions * PROGRESS_INCREMENT;
    }

    /**
     * Avança o contador sem permitir que ele ultrapasse o teto de progressão.
     */
    public static int nextCompletionCount(int currentCompletions) {
        if (currentCompletions >= MAX_REWARD_PROGRESS_COMPLETIONS) {
            return MAX_REWARD_PROGRESS_COMPLETIONS;
        }
        if (currentCompletions < 0) {
            return 1;
        }
        return currentCompletions + 1;
    }
}
