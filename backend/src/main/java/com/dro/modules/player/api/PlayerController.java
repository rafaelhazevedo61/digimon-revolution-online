package com.dro.modules.player.api;

import com.dro.modules.player.api.dto.response.PlayerDashboardResponse;
import com.dro.modules.player.api.dto.response.PlayerResponse;
import com.dro.modules.player.application.GetPlayerDashboardUseCase;
import com.dro.modules.player.application.GetPlayerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final GetPlayerUseCase useCase;
    private final GetPlayerDashboardUseCase dashboardUseCase;

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
}
