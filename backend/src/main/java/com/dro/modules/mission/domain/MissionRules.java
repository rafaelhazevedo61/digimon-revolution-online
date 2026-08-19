package com.dro.modules.mission.domain;

import java.time.Duration;

/**
 * Regras compartilhadas de recompensa e intervalo entre missões.
 *
 * <p>A dificuldade define tanto a experiência concedida quanto o cooldown
 * necessário antes de iniciar uma nova missão do mesmo fluxo.</p>
 */
public class MissionRules {

    /** Retorna a experiência concedida pela dificuldade da missão. */
    public static int getXp(MissionType type) {
        return switch (type) {
            case EASY -> 30;
            case NORMAL -> 50;
            case HARD -> 100;
        };
    }

    /** Retorna o cooldown aplicado pela dificuldade da missão. */
    public static Duration getCooldown(MissionType type) {
        return switch (type) {
            case EASY -> Duration.ofSeconds(5);
            case NORMAL -> Duration.ofSeconds(10);
            case HARD -> Duration.ofSeconds(20);
        };
    }
}
