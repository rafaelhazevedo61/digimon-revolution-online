package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;

public class AreaRules {

    public static Stage requiredStage(Area area) {
        return switch (area) {
            case DIGITAL_FOREST -> Stage.BABY;
            case VOLCANIC_ZONE -> Stage.ROOKIE;
            case OCEAN_DEPTHS -> Stage.CHAMPION;
            case ANCIENT_RUINS -> Stage.ULTIMATE;
        };
    }

    public static boolean isUnlocked(Stage highestStage, Area area) {
        return highestStage.ordinal() >= requiredStage(area).ordinal();
    }
}