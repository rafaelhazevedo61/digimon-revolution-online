package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.response.MissionInstanceResponse;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.MissionTeamRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
public class GetActiveMissionsUseCase {

    private final MissionInstanceRepository missionInstanceRepository;
    private final MissionDefinitionRepository missionDefinitionRepository;
    private final MissionTeamRepository missionTeamRepository;

    public GetActiveMissionsUseCase(
            MissionInstanceRepository missionInstanceRepository,
            MissionDefinitionRepository missionDefinitionRepository,
            MissionTeamRepository missionTeamRepository
    ) {
        this.missionInstanceRepository = missionInstanceRepository;
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.missionTeamRepository = missionTeamRepository;
    }

    public List<MissionInstanceResponse> execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        List<MissionInstance> missions =
                missionInstanceRepository.findByPlayerIdAndStatusIn(
                        playerId,
                        List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED)
                );

        List<MissionInstanceResponse> response = new ArrayList<>();

        for (MissionInstance mission : missions) {

            boolean statusChanged = mission.updateStatusIfFinished();

            if (statusChanged) {
                missionInstanceRepository.save(mission);
            }

            String missionName = missionDefinitionRepository.findById(mission.getMissionId())
                    .map(MissionDefinitionEntity::getName)
                    .orElse(mission.getMissionId());

            response.add(new MissionInstanceResponse(
                    mission.getId(),
                    mission.getMissionId(),
                    missionName,
                    mission.getStatus(),
                    mission.getStartedAt(),
                    mission.getEndsAt(),
                    mission.getSlotNumber(),
                    mission.getTeamId(),
                    mission.getTeamId() == null
                            ? null
                            : missionTeamRepository.findById(mission.getTeamId())
                                    .map(team -> team.getName())
                                    .orElse("Time de missão"),
                    mission.getDigimonIds()
            ));
        }

        return response;
    }
}