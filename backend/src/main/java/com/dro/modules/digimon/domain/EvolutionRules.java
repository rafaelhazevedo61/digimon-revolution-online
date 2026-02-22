package com.dro.modules.digimon.domain;

import com.dro.modules.inventory.domain.ItemType;

public class EvolutionRules {

    public static Stage nextStage(Stage current) {
        return switch (current) {
            case BABY -> Stage.ROOKIE;
            case ROOKIE -> Stage.CHAMPION;
            case CHAMPION -> Stage.ULTIMATE;
            case ULTIMATE -> Stage.MEGA;
            default -> null;
        };
    }

    public static int requiredLevel(Stage current) {
        return switch (current) {
            case BABY -> 10;
            case ROOKIE -> 25;
            case CHAMPION -> 50;
            case ULTIMATE -> 75;
            default -> Integer.MAX_VALUE;
        };
    }

    public static ItemType requiredFragment(Stage current) {
        return switch (current) {
            case ROOKIE -> ItemType.FRAGMENT_CHAMPION;
            case CHAMPION -> ItemType.FRAGMENT_ULTIMATE;
            case ULTIMATE -> ItemType.FRAGMENT_MEGA;
            default -> null; // BABY não precisa
        };
    }

    public static int requiredFragmentQuantity(Stage current) {
        return switch (current) {
            case ROOKIE -> 5;
            case CHAMPION -> 10;
            case ULTIMATE -> 20;
            default -> 0;
        };
    }

    public static double stageStatMultiplier(Stage stage) {
        return switch (stage) {
            case BABY -> 1.0;
            case ROOKIE -> 1.2;
            case CHAMPION -> 1.5;
            case ULTIMATE -> 2.0;
            case MEGA -> 2.8;
        };
    }
}
