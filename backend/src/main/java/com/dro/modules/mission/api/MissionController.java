package com.dro.modules.mission.api;

import com.dro.modules.mission.application.StartMissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final StartMissionUseCase useCase;

    @PostMapping("/start")
    public ResponseEntity<MissionResultResponse> start(
            @RequestHeader("Authorization") String authorization,
            @RequestBody StartMissionRequest request
    ) {
        return ResponseEntity.ok(
                useCase.execute(authorization, request.type())
        );
    }
}
