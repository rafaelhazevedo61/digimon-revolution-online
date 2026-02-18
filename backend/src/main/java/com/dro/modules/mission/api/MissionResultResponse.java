package com.dro.modules.mission.api;

import com.dro.modules.inventory.domain.ItemType;

public record MissionResultResponse(
        int xpGained,
        boolean levelUp,
        ItemType droppedItem
) {}