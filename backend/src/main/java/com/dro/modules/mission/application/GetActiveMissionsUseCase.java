package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.response.MissionInstanceResponse;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GetActiveMissionsUseCase {

    private final MissionInstanceRepository missionInstanceRepository;
    private final MissionDefinitionRepository missionDefinitionRepository;

    public GetActiveMissionsUseCase(
            MissionInstanceRepository missionInstanceRepository,
            MissionDefinitionRepository missionDefinitionRepository
    ) {
        this.missionInstanceRepository = missionInstanceRepository;
        this.missionDefinitionRepository = missionDefinitionRepository;
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
                    mission.getEndsAt()
            ));
        }

        return response;
    }
}