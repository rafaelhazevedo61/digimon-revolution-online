package com.dro.modules.mission.application;

import com.dro.modules.mission.api.response.MissionInstanceResponse;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
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

        UUID playerId = UUID.fromString(token.split(":")[1]);

        List<MissionInstance> missions =
                missionInstanceRepository.findByPlayerIdAndStatusIn(
                        playerId,
                        List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED)
                );

        List<MissionInstanceResponse> response = new ArrayList<>();

        for (MissionInstance mission : missions) {

            MissionStatus previousStatus = mission.getStatus();

            mission.updateStatusIfFinished();

            if (mission.updateStatusIfFinished()) {
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