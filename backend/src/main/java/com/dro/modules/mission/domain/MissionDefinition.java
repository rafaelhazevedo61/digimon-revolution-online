package com.dro.modules.mission.domain;

import com.dro.modules.inventory.domain.ItemType;

public class MissionDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final int requiredLevel;
    private final int xpReward;
    private final ItemType rewardItem;

    public MissionDefinition(
            String id,
            String name,
            String description,
            int requiredLevel,
            int xpReward,
            ItemType rewardItem
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.xpReward = xpReward;
        this.rewardItem = rewardItem;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getRequiredLevel() { return requiredLevel; }
    public int getXpReward() { return xpReward; }
    public ItemType getRewardItem() { return rewardItem; }
}
