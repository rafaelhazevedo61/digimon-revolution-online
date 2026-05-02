package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.response.MissionInstanceResponse;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GetActiveMissionsUseCase {

    private final MissionInstanceRepository missionInstanceRepository;

    public GetActiveMissionsUseCase(MissionInstanceRepository missionInstanceRepository) {
        this.missionInstanceRepository = missionInstanceRepository;
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

            response.add(new MissionInstanceResponse(
                    mission.getId(),
                    mission.getMissionId(),
                    mission.getStatus(),
                    mission.getStartedAt(),
                    mission.getEndsAt()
            ));
        }

        return response;
    }
}