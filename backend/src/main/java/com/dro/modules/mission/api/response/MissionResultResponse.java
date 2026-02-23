package com.dro.modules.mission.api.response;

import java.util.List;

public record MissionResultResponse(
        String missionId,
        int xpGained,
        boolean levelUp,
        List<RewardResponse> rewards
) {}