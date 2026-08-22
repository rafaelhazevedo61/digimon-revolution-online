package com.dro.modules.boss.world.api;

import com.dro.modules.boss.world.api.dto.response.AttackWorldBossResponse;
import com.dro.modules.boss.world.api.dto.response.WorldBossResponse;
import com.dro.modules.boss.world.application.AttackWorldBossUseCase;
import com.dro.modules.boss.world.application.GetWorldBossUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Componente da camada de controller da API do módulo de Boss Mundial.
 */
@RestController
@RequestMapping("/world-boss")
@RequiredArgsConstructor
public class WorldBossController {

    private final GetWorldBossUseCase getWorldBossUseCase;
    private final AttackWorldBossUseCase attackWorldBossUseCase;

    @GetMapping("/me")
    public ResponseEntity<WorldBossResponse> getMyWorldBoss(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(getWorldBossUseCase.execute(authorization));
    }

    @PostMapping("/attack")
    public ResponseEntity<AttackWorldBossResponse> attack(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.ok(attackWorldBossUseCase.execute(authorization, idempotencyKey));
    }
}
