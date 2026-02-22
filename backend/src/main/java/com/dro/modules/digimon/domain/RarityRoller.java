package com.dro.modules.digimon.domain;

import java.util.Random;

public class RarityRoller {

    private static final Random random = new Random();

    public static Rarity roll() {

        int roll = random.nextInt(100);

        if (roll < 2) {
            return Rarity.LEGENDARY;   // 2%
        } else if (roll < 10) {
            return Rarity.EPIC;        // 8%
        } else if (roll < 30) {
            return Rarity.RARE;        // 20%
        } else {
            return Rarity.COMMON;      // 70%
        }
    }
}