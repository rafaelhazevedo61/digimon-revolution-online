package com.dro.modules.player.api;

import com.dro.modules.player.api.dto.request.ChangeEmailRequest;
import com.dro.modules.player.api.dto.request.ChangePasswordRequest;
import com.dro.modules.player.api.dto.response.ChangeEmailResponse;
import com.dro.modules.player.api.dto.response.ChangePasswordResponse;
import com.dro.modules.player.api.dto.response.PlayerDashboardResponse;
import com.dro.modules.player.api.dto.response.PlayerResponse;
import com.dro.modules.player.api.dto.response.PlayerStartupResponse;
import com.dro.modules.player.application.ChangePlayerEmailUseCase;
import com.dro.modules.player.application.ChangePlayerPasswordUseCase;
import com.dro.modules.player.application.GetPlayerDashboardUseCase;
import com.dro.modules.player.application.GetPlayerStartupUseCase;
import com.dro.modules.player.application.GetPlayerUseCase;
import com.dro.modules.player.application.RevokePlayerSessionsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Componente da camada de controller da API do módulo de Jogadores.
 */
@RestController
@RequestMapping("/players")
public class PlayerController {
    private final GetPlayerUseCase useCase;
    private final GetPlayerDashboardUseCase dashboardUseCase;
    private final GetPlayerStartupUseCase startupUseCase;
    private final ChangePlayerEmailUseCase changePlayerEmailUseCase;
    private final ChangePlayerPasswordUseCase changePlayerPasswordUseCase;
    private final RevokePlayerSessionsUseCase revokePlayerSessionsUseCase;

    @GetMapping("/me")
    public ResponseEntity<PlayerResponse> me(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(useCase.execute(authorization));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<PlayerDashboardResponse> dashboard(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(dashboardUseCase.execute(authorization));
    }

    @GetMapping("/me/startup")
    public ResponseEntity<PlayerStartupResponse> startup(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(startupUseCase.execute(authorization));
    }

    @PostMapping("/me/change-email")
    public ResponseEntity<ChangeEmailResponse> changeEmail(@RequestHeader("Authorization") String authorization, @RequestBody @Valid ChangeEmailRequest request) {
        return ResponseEntity.ok(changePlayerEmailUseCase.execute(authorization, request));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@RequestHeader("Authorization") String authorization, @RequestBody @Valid ChangePasswordRequest request) {
        return ResponseEntity.ok(changePlayerPasswordUseCase.execute(authorization, request));
    }

    @PostMapping("/me/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestHeader("Authorization") String authorization) {
        revokePlayerSessionsUseCase.execute(authorization);
        return ResponseEntity.ok().build();
    }

    public PlayerController(final GetPlayerUseCase useCase, final GetPlayerDashboardUseCase dashboardUseCase, final GetPlayerStartupUseCase startupUseCase, final ChangePlayerEmailUseCase changePlayerEmailUseCase, final ChangePlayerPasswordUseCase changePlayerPasswordUseCase, final RevokePlayerSessionsUseCase revokePlayerSessionsUseCase) {
        this.useCase = useCase;
        this.dashboardUseCase = dashboardUseCase;
        this.startupUseCase = startupUseCase;
        this.changePlayerEmailUseCase = changePlayerEmailUseCase;
        this.changePlayerPasswordUseCase = changePlayerPasswordUseCase;
        this.revokePlayerSessionsUseCase = revokePlayerSessionsUseCase;
    }
}
