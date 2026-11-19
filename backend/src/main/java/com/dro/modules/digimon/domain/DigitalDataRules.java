package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Stage;

/** Regras de conversão de um Digimon sacrificado em moeda de conta. */
public final class DigitalDataRules {
    private DigitalDataRules() {
    }

    public static int calculate(Stage stage, int level, int ivHp, int ivAttack, int ivDefense) {
        int base = baseByStage(stage);
        int safeLevel = Math.min(Math.max(level, 1), 100);
        int averageIv = (clampIv(ivHp) + clampIv(ivAttack) + clampIv(ivDefense)) / 3;
        int levelFactor = 25 + ((75 * safeLevel) / 100);
        int ivFactor = 50 + (averageIv / 2);
        return Math.max(1, (base * levelFactor * ivFactor) / 10_000);
    }

    public static int baseByStage(Stage stage) {
        return switch (stage) {
            case BABY -> 1;
            case BABY_II -> 2;
            case ROOKIE -> 5;
            case CHAMPION -> 12;
            case ULTIMATE -> 30;
            case MEGA -> 60;
        };
    }

    private static int clampIv(int value) {
        return Math.min(Math.max(value, 0), 100);
    }
}
