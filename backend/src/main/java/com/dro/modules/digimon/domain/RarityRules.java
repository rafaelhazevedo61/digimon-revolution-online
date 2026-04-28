package com.dro.modules.digimon.domain;

public class RarityRules {

    public static double getStatMultiplier(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 1.0;
            case RARE -> 1.1;
            case EPIC -> 1.25;
            case LEGENDARY -> 1.5;
        };
    }

    public static double getXpMultiplier(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 1.0;
            case RARE -> 1.05;
            case EPIC -> 1.1;
            case LEGENDARY -> 1.2;
        };
    }

    public static int getMinimumIv(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0;
            case RARE -> 25;
            case EPIC -> 50;
            case LEGENDARY -> 75;
        };
    }
}
