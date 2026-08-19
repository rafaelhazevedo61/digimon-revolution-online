package com.dro.modules.clan.api.dto.response;

import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record PlayerClanMissionResponse(
        UUID id,
        UUID missionId,
        String code,
        String title,
        String description,
        ClanMissionObjectiveType objectiveType,
        int targetValue,
        int progress,
        PlayerClanMissionStatus status,
        int honorMarksReward,
        int clanXpReward,
        LocalDateTime acceptedAt,
        LocalDateTime completedAt
) {
}
