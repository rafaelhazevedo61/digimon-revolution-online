package com.dro.modules.mission.api;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mission.domain.MissionType;

public record MissionResultResponse(
        MissionType missionType,
        int xpGained,
        boolean levelUp,
        ItemType droppedItem
) {}