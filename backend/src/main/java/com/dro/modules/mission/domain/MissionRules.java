package com.dro.modules.mission.domain;

import java.time.Duration;

public class MissionRules {

    public static int getXp(MissionType type) {
        return switch (type) {
            case EASY -> 30;
            case NORMAL -> 50;
            case HARD -> 100;
        };
    }

    public static Duration getCooldown(MissionType type) {
        return switch (type) {
            case EASY -> Duration.ofSeconds(5);
            case NORMAL -> Duration.ofSeconds(10);
            case HARD -> Duration.ofSeconds(20);
        };
    }
}
