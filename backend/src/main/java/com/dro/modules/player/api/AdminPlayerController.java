package com.dro.modules.player.api;

import com.dro.modules.player.api.dto.request.ResetPlayerPasswordRequest;
import com.dro.modules.player.api.dto.response.AdminPlayerPageResponse;
import com.dro.modules.player.api.dto.response.ResetPlayerPasswordResponse;
import com.dro.modules.player.application.GetAdminPlayersUseCase;
import com.dro.modules.player.application.ResetPlayerPasswordUseCase;
import com.dro.modules.player.application.WipePlayerDataUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/players")
@RequiredArgsConstructor
public class AdminPlayerController {

    private final GetAdminPlayersUseCase getAdminPlayersUseCase;
    private final WipePlayerDataUseCase wipePlayerDataUseCase;
    private final ResetPlayerPasswordUseCase resetPlayerPasswordUseCase;

    @GetMapping
    public ResponseEntity<AdminPlayerPageResponse> getPlayers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String selectedDigitama,
            @RequestParam(required = false) Boolean starterSelected,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                normalizePageSize(size),
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.asc("username")
                )
        );

        return ResponseEntity.ok(
                getAdminPlayersUseCase.execute(
                        username,
                        email,
                        selectedDigitama,
                        starterSelected,
                        pageRequest
                )
        );
    }

    @PostMapping("/wipe")
    public ResponseEntity<Void> wipe() {
        wipePlayerDataUseCase.execute();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ResetPlayerPasswordResponse> resetPassword(
            @PathVariable UUID id,
            @RequestBody @Valid ResetPlayerPasswordRequest request
    ) {
        return ResponseEntity.ok(resetPlayerPasswordUseCase.execute(id, request));
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }
}