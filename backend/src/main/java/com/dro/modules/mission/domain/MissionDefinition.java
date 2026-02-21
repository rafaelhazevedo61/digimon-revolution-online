package com.dro.modules.mission.domain;

import com.dro.modules.inventory.domain.ItemType;

import java.util.List;

public class MissionDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final int requiredLevel;
    private final int baseXp;
    List<MissionReward> rewards;

    public MissionDefinition(
            String id,
            String name,
            String description,
            int requiredLevel,
            int baseXp,
            List<MissionReward> rewards
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.baseXp = baseXp;
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
}
