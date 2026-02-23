package com.dro.modules.mission.domain;

import java.util.List;

public class MissionDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final Area area;
    private final int requiredLevel;
    private final int baseXp;
    private final int energyCost;
    private final int durationSeconds;
    private List<MissionReward> rewards;

    public MissionDefinition (
            String id,
            String name,
            String description, Area area,
            int requiredLevel,
            int baseXp, int energyCost, int durationSeconds,
            List<MissionReward> rewards
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.area = area;
        this.requiredLevel = requiredLevel;
        this.baseXp = baseXp;
        this.energyCost = energyCost;
        this.durationSeconds = durationSeconds;
        this.rewards = rewards;
    }

    public String getId () {
        return id;
    }

    public String getName () {
        return name;
    }

    public String getDescription () {
        return description;
    }

    public int getRequiredLevel () {
        return requiredLevel;
    }

    public int getBaseXp () {
        return baseXp;
    }

    public List<MissionReward> getRewards () {
        return rewards;
    }

    public void setRewards (List<MissionReward> rewards) {
        this.rewards = rewards;
    }

    public Area getArea () {
        return area;
    }

    public int getEnergyCost () {
        return energyCost;
    }

    public int getDurationSeconds () {
        return durationSeconds;
    }
}
