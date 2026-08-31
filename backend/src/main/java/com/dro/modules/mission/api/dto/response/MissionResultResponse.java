package com.dro.modules.mission.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Missões.
 */
public record MissionResultResponse(
        String missionId,
        int xpGained,
        int bitsGained,
        boolean levelUp,
        List<RewardResponse> rewards,
        NewlyUnlockedContentResponse newlyUnlockedContent,
        MissionRewardBreakdownResponse experienceBreakdown,
        MissionRewardBreakdownResponse bitsBreakdown
) {}