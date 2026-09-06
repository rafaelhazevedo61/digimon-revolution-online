package com.dro.modules.mission.application;

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

    public SetMissionAutoClaimUseCase(MissionInstanceRepository missionInstanceRepository) {
        this.missionInstanceRepository = missionInstanceRepository;
    }

    @Transactional
    public MissionAutoRepeatResponse execute(String token, UUID missionInstanceId, boolean enabled) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        MissionInstance instance = missionInstanceRepository.findByIdAndPlayerId(missionInstanceId, playerId)
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
