package com.dro.modules.digimon.domain;

import java.util.Random;

public class PersonalityRoller {

    private static final Random random = new Random();

    public static Personality roll() {
        Personality[] values = Personality.values();
        return values[random.nextInt(values.length)];
    }
}