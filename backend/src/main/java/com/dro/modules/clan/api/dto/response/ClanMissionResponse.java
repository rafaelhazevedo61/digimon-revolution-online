package com.dro.modules.clan.api.dto.response;

import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanMissionResponse(
        UUID id,
        String code,
        String title,
        String description,
        ClanMissionObjectiveType objectiveType,
        int targetValue,
        int minHonorMarksReward,
        int maxHonorMarksReward,
        int clanXpReward,
        int minClanLevel,
        boolean alreadyAccepted
) {
}
