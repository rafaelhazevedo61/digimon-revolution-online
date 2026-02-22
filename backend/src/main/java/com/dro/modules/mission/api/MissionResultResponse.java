package com.dro.modules.mission.api;

import com.dro.modules.inventory.domain.ItemType;

import java.util.List;

public record MissionResultResponse(
        String missionId,
        int xpGained,
        boolean levelUp,
        List<RewardResponse> rewards
) {}