package com.dro.modules.player.api;

import com.dro.modules.player.api.dto.request.ResetPlayerPasswordRequest;
import com.dro.modules.player.api.dto.request.WipePlayersRequest;
import com.dro.modules.player.api.dto.response.AdminPlayerPageResponse;
import com.dro.modules.player.api.dto.response.ResetPlayerPasswordResponse;
import com.dro.modules.player.application.GetAdminPlayersUseCase;
import com.dro.modules.player.application.ResetPlayerPasswordUseCase;
import com.dro.modules.player.application.WipePlayerDataUseCase;
import com.dro.modules.player.domain.VipPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.UUID;

import com.dro.modules.player.api.dto.response.AdminPlayerResponse;
import com.dro.modules.player.application.SetPlayerVipUseCase;

/**
 * Componente da camada de controller da API do módulo de Jogadores.
 */
@RestController
@RequestMapping("/admin/players")
public class AdminPlayerController {
    private final GetAdminPlayersUseCase getAdminPlayersUseCase;
    private final WipePlayerDataUseCase wipePlayerDataUseCase;
    private final ResetPlayerPasswordUseCase resetPlayerPasswordUseCase;

    private final SetPlayerVipUseCase setPlayerVipUseCase;

    @PutMapping("/{id}/vip")
    public AdminPlayerResponse setVip(@RequestHeader("Authorization") String authorization, @PathVariable UUID id,
                                      @RequestBody VipRequest request) {
        return setPlayerVipUseCase.execute(authorization, id, request.expiresAt());
    }

    @PostMapping("/{id}/vip/extend")
    public AdminPlayerResponse extendVip(@RequestHeader("Authorization") String authorization, @PathVariable UUID id,
                                         @RequestBody @Valid VipExtensionRequest request) {
        return setPlayerVipUseCase.extend(authorization, id, request.plan());
    }

    public record VipExtensionRequest(@NotNull VipPlan plan) {}

    public record VipRequest(Instant expiresAt) {}

    @GetMapping
    public ResponseEntity<AdminPlayerPageResponse> getPlayers(@RequestParam(required = false) String username, @RequestParam(required = false) String email, @RequestParam(required = false) String selectedDigitama, @RequestParam(required = false) Boolean starterSelected, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), normalizePageSize(size), Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("username")));
        return ResponseEntity.ok(getAdminPlayersUseCase.execute(username, email, selectedDigitama, starterSelected, pageRequest));
    }

    @PostMapping("/wipe")
    public ResponseEntity<Void> wipe(@RequestHeader("Authorization") String authorization, @RequestBody WipePlayersRequest request) {
        wipePlayerDataUseCase.execute(authorization, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ResetPlayerPasswordResponse> resetPassword(@RequestHeader("Authorization") String authorization, @PathVariable UUID id, @RequestBody @Valid ResetPlayerPasswordRequest request) {
        return ResponseEntity.ok(resetPlayerPasswordUseCase.execute(authorization, id, request));
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 100);
    }

    public AdminPlayerController(final GetAdminPlayersUseCase getAdminPlayersUseCase, final WipePlayerDataUseCase wipePlayerDataUseCase, final ResetPlayerPasswordUseCase resetPlayerPasswordUseCase, final SetPlayerVipUseCase setPlayerVipUseCase) {
        this.setPlayerVipUseCase = setPlayerVipUseCase;
        this.getAdminPlayersUseCase = getAdminPlayersUseCase;
        this.wipePlayerDataUseCase = wipePlayerDataUseCase;
        this.resetPlayerPasswordUseCase = resetPlayerPasswordUseCase;
    }
}
