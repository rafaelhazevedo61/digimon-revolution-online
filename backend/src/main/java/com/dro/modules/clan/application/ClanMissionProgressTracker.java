package com.dro.modules.clan.application;

import com.dro.modules.clan.domain.ClanMission;
import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import com.dro.modules.clan.infra.ClanMissionRepository;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClanMissionProgressTracker {

    private final PlayerClanMissionRepository playerClanMissionRepository;
    private final ClanMissionRepository clanMissionRepository;

    @Transactional
    public void track(UUID playerId, ClanMissionObjectiveType objectiveType) {
        PlayerClanMission active = playerClanMissionRepository
                .findByPlayerIdAndStatus(playerId, PlayerClanMissionStatus.IN_PROGRESS)
                .orElse(null);

        if (active == null) {
            return;
        }

        ClanMission mission = clanMissionRepository.findById(active.getClanMissionId())
                .orElse(null);

        if (mission == null || mission.getObjectiveType() != objectiveType) {
            return;
        }

        int newProgress = active.getProgress() + 1;
        active.setProgress(newProgress);

        if (newProgress >= mission.getTargetValue()) {
            active.setStatus(PlayerClanMissionStatus.COMPLETED);
        }

        playerClanMissionRepository.save(active);
    }
}
