package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Trait;

public class TraitRules {

    public static double getXpMultiplier(Trait trait) {
        if (trait == Trait.FAST_LEARNER) {
            return 1.10;
        }

        return 1.0;
    }

    public static double getHpMultiplier(Trait trait) {
        if (trait == Trait.VITALITY) {
            return 1.10;
        }

        return 1.0;
    }

    public static double getAttackMultiplier(Trait trait) {
        if (trait == Trait.BERSERKER) {
            return 1.10;
        }

        return 1.0;
    }

    public static double getDefenseMultiplier(Trait trait) {
        if (trait == Trait.IRON_BODY) {
            return 1.10;
        }

        return 1.0;
    }

    public static int getMaxEnergyBonus(Trait trait) {
        if (trait == Trait.ENERGETIC) {
            return 5;
        }

        return 0;
    }
}