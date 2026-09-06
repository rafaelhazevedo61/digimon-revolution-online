package com.dro.modules.mission.api.dto.response;

import com.dro.modules.mission.domain.MissionTeam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MissionTeamResponse(
        UUID id,
        String name,
        List<UUID> digimonIds,
        UUID captainDigimonId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MissionTeamResponse from(MissionTeam team) {
        return new MissionTeamResponse(
                team.getId(),
                team.getName(),
                team.getDigimonIds(),
                team.getCaptainDigimonId(),
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }
}
