package com.dro.modules.player.api;

import com.dro.modules.player.api.dto.request.ChangePasswordRequest;
import com.dro.modules.player.api.dto.response.PlayerDashboardResponse;
import com.dro.modules.player.api.dto.response.PlayerResponse;
import com.dro.modules.player.api.dto.response.PlayerStartupResponse;
import com.dro.modules.player.application.ChangePlayerPasswordUseCase;
import com.dro.modules.player.application.GetPlayerDashboardUseCase;
import com.dro.modules.player.application.GetPlayerStartupUseCase;
import com.dro.modules.player.application.GetPlayerUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final GetPlayerUseCase useCase;
    private final GetPlayerDashboardUseCase dashboardUseCase;
    private final GetPlayerStartupUseCase startupUseCase;
    private final ChangePlayerPasswordUseCase changePlayerPasswordUseCase;

    @GetMapping("/me")
    public ResponseEntity<PlayerResponse> me(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(useCase.execute(authorization));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<PlayerDashboardResponse> dashboard(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(dashboardUseCase.execute(authorization));
    }

    @GetMapping("/me/startup")
    public ResponseEntity<PlayerStartupResponse> startup(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(startupUseCase.execute(authorization));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        changePlayerPasswordUseCase.execute(authorization, request);
        return ResponseEntity.ok().build();
    }
}
