package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Personality;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Digimon.
 */
public class PersonalityRules {

    public static double getHpMultiplier(Personality personality) {
        return switch (personality) {
            case DURABLE -> 1.10;
            default -> 1.0;
        };
    }

    public static double getAttackMultiplier(Personality personality) {
        return switch (personality) {
            case FIGHTER -> 1.10;
            case BRAINY -> 1.05;
            case NIMBLE -> 1.05;
            default -> 1.0;
        };
    }

    public static double getDefenseMultiplier(Personality personality) {
        return switch (personality) {
            case DEFENDER -> 1.10;
            case NIMBLE -> 1.05;
            default -> 1.0;
        };
    }

    public static double getXpMultiplier(Personality personality) {
        return switch (personality) {
            case LIVELY -> 1.10;
            case BRAINY -> 1.05;
            default -> 1.0;
        };
    }
}
