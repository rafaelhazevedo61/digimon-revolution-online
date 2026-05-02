package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;

public class AreaRules {

    public static Stage requiredStage(Area area) {
        return switch (area) {
            case NATIVE_FOREST -> Stage.BABY;
            case GEAR_SAVANNA, FACTORIAL_TOWN -> Stage.ROOKIE;
            case FREEZELAND -> Stage.CHAMPION;
            case SERVER_DESERT -> Stage.ULTIMATE;
            case INFINITY_MOUNTAIN -> Stage.MEGA;
        };
    }

    public static boolean isUnlocked(Stage highestStage, Area area) {
        return highestStage.ordinal() >= requiredStage(area).ordinal();
    }
}