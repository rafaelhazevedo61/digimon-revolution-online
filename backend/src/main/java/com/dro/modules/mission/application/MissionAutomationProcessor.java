package com.dro.modules.mission.application;

import com.dro.modules.player.application.VipAccess;
import com.dro.modules.mission.api.dto.response.MissionResultResponse;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Processa uma missão automática em uma transação isolada. */
@Service
public class MissionAutomationProcessor {
    private final VipAccess vipAccess;
    private final MissionInstanceRepository missionInstanceRepository;
    private final ClaimMissionUseCase claimMissionUseCase;
    private final StartMissionUseCase startMissionUseCase;

    public MissionAutomationProcessor(
            VipAccess vipAccess,
            MissionInstanceRepository missionInstanceRepository,
            ClaimMissionUseCase claimMissionUseCase,
            StartMissionUseCase startMissionUseCase
    ) {
        this.vipAccess = vipAccess;
        this.missionInstanceRepository = missionInstanceRepository;
        this.claimMissionUseCase = claimMissionUseCase;
        this.startMissionUseCase = startMissionUseCase;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(UUID missionInstanceId) {
        UUID ownerId = missionInstanceRepository.findOwnerId(missionInstanceId).orElse(null);
        if (ownerId == null) return;
        var player = vipAccess.lockPlayer(ownerId);
        MissionInstance instance = missionInstanceRepository.findByIdForUpdate(missionInstanceId).orElse(null);
        if (instance == null || !instance.isAutoClaimEnabled() || instance.isAlreadyClaimed()
                || instance.getEndsAt().isAfter(java.time.Instant.now())) {
            return;
        }

        if (!player.hasActiveVip(Instant.now())) {
            instance.pauseAutomation(VipAccess.PAUSE_REASON, VipAccess.PAUSE_REASON);
            missionInstanceRepository.save(instance);
            return;
        }

        MissionResultResponse result = claimMissionUseCase.executeForPlayer(instance.getPlayerId(), missionInstanceId);
        if (result.autoRepeatEnabled() && result.teamId() != null) {
            startMissionUseCase.executeForPlayer(
                    instance.getPlayerId(),
                    result.missionId(),
                    result.teamId(),
                    true,
                    result.autoClaimEnabled()
            );
        }
    }

    @Transactional
    public void pauseAutomation(UUID missionInstanceId) {
        pauseAutomation(missionInstanceId, "MANUAL", "MANUAL");
    }

    @Transactional
    public void pauseAutomation(UUID missionInstanceId, String reason, String errorCode) {
        MissionInstance instance = missionInstanceRepository.findByIdForUpdate(missionInstanceId).orElse(null);
        if (instance == null || instance.isAlreadyClaimed()) return;
        instance.pauseAutomation(reason, errorCode);
        missionInstanceRepository.save(instance);
    }
}
