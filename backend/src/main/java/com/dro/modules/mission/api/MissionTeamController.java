package com.dro.modules.mission.api;

import com.dro.modules.mission.api.dto.request.SaveMissionTeamRequest;
import com.dro.modules.mission.api.dto.response.MissionTeamResponse;
import com.dro.modules.mission.application.MissionTeamUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mission-teams")
public class MissionTeamController {
    private final MissionTeamUseCase missionTeamUseCase;

    @GetMapping
    public ResponseEntity<List<MissionTeamResponse>> list(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(missionTeamUseCase.list(authorization));
    }

    @PostMapping
    public ResponseEntity<MissionTeamResponse> create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SaveMissionTeamRequest request
    ) {
        return ResponseEntity.ok(missionTeamUseCase.create(authorization, request));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<MissionTeamResponse> update(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID teamId,
            @RequestBody @Valid SaveMissionTeamRequest request
    ) {
        return ResponseEntity.ok(missionTeamUseCase.update(authorization, teamId, request));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> delete(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID teamId
    ) {
        missionTeamUseCase.delete(authorization, teamId);
        return ResponseEntity.noContent().build();
    }

    public MissionTeamController(MissionTeamUseCase missionTeamUseCase) {
        this.missionTeamUseCase = missionTeamUseCase;
    }
}
