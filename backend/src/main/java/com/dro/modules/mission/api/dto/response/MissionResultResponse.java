package com.dro.modules.mission.api.dto.response;

import java.util.List;

public record MissionResultResponse(
        String missionId,
        int xpGained,
        int bitsGained,
        boolean levelUp,
        List<RewardResponse> rewards
) {}