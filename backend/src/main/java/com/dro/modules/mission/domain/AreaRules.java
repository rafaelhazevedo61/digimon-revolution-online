package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Missões.
 */
public class AreaRules {

    public static final String GEAR_SAVANNA_ENTRY_MISSION = "MISSION_2";

    public static boolean meetsMissionRequirements(Stage stage, int level, MissionDefinitionEntity mission) {
        return stage != null && stage.ordinal() >= mission.getRequiredStage().ordinal()
                && level >= mission.getRequiredLevel();
    }

    public static Stage requiredStage(Area area) {
        return switch (area) {
            case NATIVE_FOREST -> Stage.BABY;
            case GEAR_SAVANNA -> Stage.BABY; // The entry mission defines the actual requirements.
            case FACTORIAL_TOWN -> Stage.ROOKIE;
            case FREEZELAND -> Stage.CHAMPION;
            case SERVER_DESERT -> Stage.ULTIMATE;
            case INFINITY_MOUNTAIN -> Stage.MEGA;
        };
    }

    public static boolean isUnlocked(Stage highestStage, Area area) {
        return highestStage.ordinal() >= requiredStage(area).ordinal();
    }
}