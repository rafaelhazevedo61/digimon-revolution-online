package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Trait;

import java.util.Random;

public class TraitRoller {

    private static final Random random = new Random();

    private static final double NORMAL_HATCH_TRAIT_CHANCE = 0.05;
    private static final double REBIRTH_BASE_TRAIT_CHANCE = 0.10;
    private static final double REBIRTH_TRAIT_CHANCE_PER_REBIRTH = 0.02;
    private static final double MAX_REBIRTH_TRAIT_CHANCE = 0.40;

    public static Trait rollForNormalHatch() {

        if (random.nextDouble() > NORMAL_HATCH_TRAIT_CHANCE) {
            return null;
        }

        return rollTrait();
    }

    public static Trait rollForRebirth(int rebirthCount) {

        double chance = REBIRTH_BASE_TRAIT_CHANCE
                + (rebirthCount * REBIRTH_TRAIT_CHANCE_PER_REBIRTH);

        chance = Math.min(chance, MAX_REBIRTH_TRAIT_CHANCE);

        if (random.nextDouble() > chance) {
            return null;
        }

        return rollTrait();
    }

    private static Trait rollTrait() {
        Trait[] traits = Trait.values();
        return traits[random.nextInt(traits.length)];
    }
}