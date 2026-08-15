package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanMissionResponse;
import com.dro.modules.clan.api.dto.response.PlayerClanMissionResponse;
import com.dro.modules.clan.domain.ClanMission;
import com.dro.modules.clan.domain.PlayerClanMission;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ClanMissionResponseMapper {

    public ClanMissionResponse toCatalog(ClanMission mission, boolean alreadyAccepted) {
        return new ClanMissionResponse(
                mission.getId(),
                mission.getCode(),
                mission.getTitle(),
                mission.getDescription(),
                mission.getObjectiveType(),
                mission.getTargetValue(),
                mission.getMinHonorMarksReward(),
                mission.getMaxHonorMarksReward(),
                mission.getClanXpReward(),
                mission.getMinClanLevel(),
                alreadyAccepted
        );
    }

    public PlayerClanMissionResponse toPlayerMission(PlayerClanMission playerMission, ClanMission mission) {
        return new PlayerClanMissionResponse(
                playerMission.getId(),
                mission.getId(),
                mission.getCode(),
                mission.getTitle(),
                mission.getDescription(),
                mission.getObjectiveType(),
                mission.getTargetValue(),
                playerMission.getProgress(),
                playerMission.getStatus(),
                playerMission.getHonorMarksReward(),
                mission.getClanXpReward(),
                playerMission.getAcceptedAt(),
                playerMission.getCompletedAt()
        );
    }

    public PlayerClanMissionResponse toPlayerMission(PlayerClanMission playerMission) {
        return toPlayerMission(playerMission, emptyMission(playerMission.getClanMissionId()));
    }

    private ClanMission emptyMission(UUID id) {
        return ClanMission.builder()
                .id(id)
                .code("UNKNOWN")
                .title("Unknown")
                .description(null)
                .objectiveType(null)
                .targetValue(0)
                .minHonorMarksReward(0)
                .maxHonorMarksReward(0)
                .clanXpReward(0)
                .minClanLevel(1)
                .build();
    }
}
