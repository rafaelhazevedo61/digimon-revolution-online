package com.dro.modules.mission.api.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Missões.
 */
public record MissionResultResponse(
        String missionId,
        UUID teamId,
        int xpGained,
        int bitsGained,
        boolean levelUp,
        List<RewardResponse> rewards,
        NewlyUnlockedContentResponse newlyUnlockedContent,
        MissionRewardBreakdownResponse experienceBreakdown,
        MissionRewardBreakdownResponse bitsBreakdown
) {}