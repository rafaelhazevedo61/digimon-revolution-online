package com.dro.modules.clan.application;

import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Expira missões de clã que permaneceram em andamento
 * após o fim do dia em que foram aceitas.
 */
@Service
@RequiredArgsConstructor
public class ExpireClanMissionsUseCase {

    private final PlayerClanMissionRepository playerClanMissionRepository;

    public ExpireClanMissionsUseCase (PlayerClanMissionRepository playerClanMissionRepository) {
        this.playerClanMissionRepository = playerClanMissionRepository;
    }

    @Transactional
    public int execute(LocalDateTime cutoff) {
        return playerClanMissionRepository.expireInProgressAcceptedBefore(
                cutoff,
                PlayerClanMissionStatus.IN_PROGRESS,
                PlayerClanMissionStatus.EXPIRED
        );
    }
}