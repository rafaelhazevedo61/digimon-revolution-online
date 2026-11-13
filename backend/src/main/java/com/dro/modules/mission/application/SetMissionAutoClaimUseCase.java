package com.dro.modules.mission.application;

import com.dro.modules.player.application.VipAccess;
import com.dro.modules.mission.api.dto.response.MissionAutoRepeatResponse;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Liga ou pausa o resgate automático de uma missão pertencente ao jogador.
 */
@Service
public class SetMissionAutoClaimUseCase {
    private final MissionInstanceRepository missionInstanceRepository;

    private final VipAccess vipAccess;

    public SetMissionAutoClaimUseCase(MissionInstanceRepository missionInstanceRepository, VipAccess vipAccess) {
        this.vipAccess = vipAccess;
        this.missionInstanceRepository = missionInstanceRepository;
    }

    @Transactional
    public MissionAutoRepeatResponse execute(String token, UUID missionInstanceId, boolean enabled) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        var player = vipAccess.lockPlayer(playerId);
        if (enabled) VipAccess.requireActive(player);
        MissionInstance instance = missionInstanceRepository.findByIdForUpdate(missionInstanceId)
                .filter(mission -> playerId.equals(mission.getPlayerId()))
                .orElseThrow(() -> new NotFoundException("Missão não encontrada"));

        instance.updateStatusIfFinished();
        if (instance.isAlreadyClaimed()) {
            throw new BadRequestException("A missão já foi resgatada");
        }
        if (enabled && instance.getTeamId() == null) {
            throw new BadRequestException("O modo automático exige um time de missão");
        }

        instance.setAutoClaimEnabled(enabled);
        if (enabled) {
            instance.setAutoRepeatEnabled(true);
            instance.clearAutomationPause();
        }
        missionInstanceRepository.save(instance);
        return new MissionAutoRepeatResponse(instance.getId(), instance.getSlotNumber(), enabled);
    }
}
