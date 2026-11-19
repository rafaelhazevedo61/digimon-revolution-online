package com.dro.modules.clan.application;

import com.dro.modules.clan.domain.ClanMission;
import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import com.dro.modules.clan.infra.ClanMissionRepository;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class AdminCompleteClanMissionsUseCase {
    private final PlayerClanMissionRepository playerClanMissionRepository;
    private final ClanMissionRepository clanMissionRepository;

    @Transactional
    public long execute() {
        List<PlayerClanMission> active = playerClanMissionRepository.findByStatus(PlayerClanMissionStatus.IN_PROGRESS);
        LocalDateTime now = LocalDateTime.now();
        for (PlayerClanMission playerMission : active) {
            ClanMission mission = clanMissionRepository.findById(playerMission.getClanMissionId()).orElse(null);
            if (mission == null) {
                continue;
            }
            playerMission.setProgress(mission.getTargetValue());
            playerMission.setStatus(PlayerClanMissionStatus.COMPLETED);
            playerMission.setCompletedAt(now);
        }
        playerClanMissionRepository.saveAll(active);
        return active.size();
    }

    public AdminCompleteClanMissionsUseCase(final PlayerClanMissionRepository playerClanMissionRepository, final ClanMissionRepository clanMissionRepository) {
        this.playerClanMissionRepository = playerClanMissionRepository;
        this.clanMissionRepository = clanMissionRepository;
    }
}
