package com.dro.modules.mission.api;

import com.dro.modules.inventory.domain.ItemType;

public record MissionResultResponse(
        String missionId,
        int xpGained,
        boolean levelUp,
        ItemType droppedItem
) {}