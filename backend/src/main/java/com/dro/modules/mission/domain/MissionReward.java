package com.dro.modules.mission.domain;

import com.dro.modules.inventory.domain.ItemType;

public class MissionReward {

    private final ItemType itemType;
    private final int baseQuantity;

    public MissionReward(ItemType itemType, int baseQuantity) {
        this.itemType = itemType;
        this.baseQuantity = baseQuantity;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public int getBaseQuantity() {
        return baseQuantity;
    }
}
