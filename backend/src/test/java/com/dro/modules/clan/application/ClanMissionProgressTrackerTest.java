package com.dro.modules.clan.application;

import com.dro.modules.clan.domain.ClanMission;
import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import com.dro.modules.clan.infra.ClanMissionRepository;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanMissionProgressTrackerTest {

    @Mock private PlayerClanMissionRepository playerClanMissionRepository;
    @Mock private ClanMissionRepository clanMissionRepository;

    @InjectMocks
    private ClanMissionProgressTracker tracker;

    @Test
    void track_incrementsProgressAndCompletesMission() {
        UUID playerId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();

        UUID clanId = UUID.randomUUID();
        PlayerClanMission active = PlayerClanMission.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .clanId(clanId)
                .clanMissionId(missionId)
                .status(PlayerClanMissionStatus.IN_PROGRESS)
                .progress(1)
                .acceptedAt(LocalDateTime.now())
                .build();

        ClanMission mission = ClanMission.builder()
                .id(missionId)
                .code("DEFEAT_BOSSES")
                .title("Derrotar Bosses")
                .objectiveType(ClanMissionObjectiveType.BOSSES_DEFEATED)
                .targetValue(3)
                .minHonorMarksReward(10)
                .maxHonorMarksReward(20)
                .clanXpReward(50)
                .build();

        when(playerClanMissionRepository.findByPlayerIdAndStatus(playerId, PlayerClanMissionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(active));
        when(clanMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        tracker.track(playerId, ClanMissionObjectiveType.BOSSES_DEFEATED);

        assertEquals(2, active.getProgress());
        assertEquals(PlayerClanMissionStatus.IN_PROGRESS, active.getStatus());

        tracker.track(playerId, ClanMissionObjectiveType.BOSSES_DEFEATED);

        assertEquals(3, active.getProgress());
        assertEquals(PlayerClanMissionStatus.COMPLETED, active.getStatus());
        verify(playerClanMissionRepository, times(2)).save(active);
    }

    @Test
    void track_ignoresDifferentObjective() {
        UUID playerId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();

        UUID clanId = UUID.randomUUID();
        PlayerClanMission active = PlayerClanMission.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .clanId(clanId)
                .clanMissionId(missionId)
                .status(PlayerClanMissionStatus.IN_PROGRESS)
                .progress(0)
                .acceptedAt(LocalDateTime.now())
                .build();

        ClanMission mission = ClanMission.builder()
                .id(missionId)
                .code("COMPLETE_MISSIONS")
                .title("Completar Missões")
                .objectiveType(ClanMissionObjectiveType.MISSIONS_COMPLETED)
                .targetValue(5)
                .minHonorMarksReward(10)
                .maxHonorMarksReward(20)
                .clanXpReward(50)
                .build();

        when(playerClanMissionRepository.findByPlayerIdAndStatus(playerId, PlayerClanMissionStatus.IN_PROGRESS))
                .thenReturn(Optional.of(active));
        when(clanMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        tracker.track(playerId, ClanMissionObjectiveType.BOSSES_DEFEATED);

        assertEquals(0, active.getProgress());
        verify(playerClanMissionRepository, never()).save(any());
    }

    @Test
    void track_doesNothingWhenNoActiveMission() {
        UUID playerId = UUID.randomUUID();

        when(playerClanMissionRepository.findByPlayerIdAndStatus(playerId, PlayerClanMissionStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        tracker.track(playerId, ClanMissionObjectiveType.ARENA_WINS);

        verify(clanMissionRepository, never()).findById(any());
    }
}
