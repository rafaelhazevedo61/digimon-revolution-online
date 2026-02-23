package com.dro.modules.mission.api;

import com.dro.modules.mission.api.response.MissionResponse;
import com.dro.modules.mission.api.response.MissionResultResponse;
import com.dro.modules.mission.api.request.StartMissionRequest;
import com.dro.modules.mission.application.GetAvailableMissionsUseCase;
import com.dro.modules.mission.application.StartMissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final StartMissionUseCase startMissionUseCase;
    private final GetAvailableMissionsUseCase getAvailableMissionsUseCase;

    @PostMapping("/start")
    public ResponseEntity<MissionResultResponse> start(
            @RequestHeader("Authorization") String authorization,
            @RequestBody StartMissionRequest request
    ) {
        return ResponseEntity.ok(
                startMissionUseCase.execute(authorization, request.missionId())
        );
    }

    @GetMapping
    public ResponseEntity<List<MissionResponse>> list(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(getAvailableMissionsUseCase.execute(authorization));
    }
}
