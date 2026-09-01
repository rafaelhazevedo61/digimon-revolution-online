package com.dro.modules.player.api.dto.response;

import com.dro.modules.mission.domain.MissionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record ActiveMissionResponse(
        UUID instanceId,
        String missionId,
        String missionName,
        MissionStatus status,
        Instant startedAt,
        Instant endsAt,
        long remainingSeconds,
        UUID teamId,
        String teamName,
        List<UUID> digimonIds
) {}
