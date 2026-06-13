package com.dro.modules.mission.api;

import com.dro.modules.mission.api.dto.request.CreateMissionRequest;
import com.dro.modules.mission.api.dto.request.UpdateMissionRequest;
import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.application.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/missions")
@RequiredArgsConstructor
public class AdminMissionController {

    private final CreateMissionUseCase createMissionUseCase;
    private final ListMissionsUseCase listMissionsUseCase;
    private final GetMissionUseCase getMissionUseCase;
    private final UpdateMissionUseCase updateMissionUseCase;
    private final ToggleMissionUseCase toggleMissionUseCase;

    @PostMapping
    public ResponseEntity<AdminMissionResponse> create(
            @RequestBody @Valid CreateMissionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createMissionUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<AdminMissionResponse>> list(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String lootItemType
    ) {
        return ResponseEntity.ok(listMissionsUseCase.execute(activeOnly, area, stage, lootItemType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminMissionResponse> getById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(getMissionUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminMissionResponse> update(
            @PathVariable String id,
            @RequestBody @Valid UpdateMissionRequest request
    ) {
        return ResponseEntity.ok(updateMissionUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<AdminMissionResponse> toggleActive(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(toggleMissionUseCase.execute(id));
    }
}
