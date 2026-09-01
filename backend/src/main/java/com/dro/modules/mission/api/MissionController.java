package com.dro.modules.mission.api;

import com.dro.modules.mission.api.dto.response.MissionInstanceResponse;
import com.dro.modules.mission.api.dto.response.MissionLootPreviewResponse;
import com.dro.modules.mission.api.dto.response.MissionResponse;
import com.dro.modules.mission.api.dto.response.MissionResultResponse;
import com.dro.modules.mission.api.dto.request.StartMissionRequest;
import com.dro.modules.mission.api.dto.response.MissionStartResponse;
import com.dro.modules.mission.application.ClaimMissionUseCase;
import com.dro.modules.mission.application.GetActiveMissionsUseCase;
import com.dro.modules.mission.application.GetAvailableMissionsUseCase;
import com.dro.modules.mission.application.GetMissionLootPreviewUseCase;
import com.dro.modules.mission.application.StartMissionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Missões.
 */
@RestController
@RequestMapping("/missions")
public class MissionController {
    private final StartMissionUseCase startMissionUseCase;
    private final GetAvailableMissionsUseCase getAvailableMissionsUseCase;
    private final GetMissionLootPreviewUseCase getMissionLootPreviewUseCase;
    private final ClaimMissionUseCase claimMissionUseCase;
    private final GetActiveMissionsUseCase getActiveMissionsUseCase;

    @PostMapping("/start")
    public ResponseEntity<MissionStartResponse> start(@RequestHeader("Authorization") String authorization, @RequestBody StartMissionRequest request) {
        return ResponseEntity.ok(startMissionUseCase.execute(authorization, request.missionId(), request.teamId()));
    }

    @PostMapping("/{missionInstanceId}/claim")
    public MissionResultResponse claimMission(@RequestHeader("Authorization") String authorization, @PathVariable UUID missionInstanceId) {
        return claimMissionUseCase.execute(authorization, missionInstanceId);
    }

    @GetMapping("/active")
    public List<MissionInstanceResponse> getActiveMissions(@RequestHeader("Authorization") String token) {
        return getActiveMissionsUseCase.execute(token);
    }

    @GetMapping("/{missionId}/loot")
    public MissionLootPreviewResponse getLootPreview(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String missionId
    ) {
        return getMissionLootPreviewUseCase.execute(authorization, missionId);
    }

    @GetMapping
    public ResponseEntity<List<MissionResponse>> list(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(getAvailableMissionsUseCase.execute(authorization));
    }

    public MissionController(
            final StartMissionUseCase startMissionUseCase,
            final GetAvailableMissionsUseCase getAvailableMissionsUseCase,
            final GetMissionLootPreviewUseCase getMissionLootPreviewUseCase,
            final ClaimMissionUseCase claimMissionUseCase,
            final GetActiveMissionsUseCase getActiveMissionsUseCase
    ) {
        this.startMissionUseCase = startMissionUseCase;
        this.getAvailableMissionsUseCase = getAvailableMissionsUseCase;
        this.getMissionLootPreviewUseCase = getMissionLootPreviewUseCase;
        this.claimMissionUseCase = claimMissionUseCase;
        this.getActiveMissionsUseCase = getActiveMissionsUseCase;
    }
}
