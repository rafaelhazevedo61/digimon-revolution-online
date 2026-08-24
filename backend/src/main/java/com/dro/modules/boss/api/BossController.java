package com.dro.modules.boss.api;

import com.dro.modules.boss.api.dto.request.ChallengeBossRequest;
import com.dro.modules.boss.api.dto.response.BossAttemptResponse;
import com.dro.modules.boss.api.dto.response.BossChallengeResponse;
import com.dro.modules.boss.api.dto.response.BossCooldownResponse;
import com.dro.modules.boss.api.dto.response.BossDefinitionResponse;
import com.dro.modules.boss.application.ChallengeBossUseCase;
import com.dro.modules.boss.application.GetAvailableBossesUseCase;
import com.dro.modules.boss.application.GetBossCooldownsUseCase;
import com.dro.modules.boss.application.GetBossHistoryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Componente da camada de controller da API do módulo de Boss Mundial.
 */
@RestController
@RequestMapping("/bosses")
public class BossController {
    private final GetAvailableBossesUseCase getAvailableBossesUseCase;
    private final ChallengeBossUseCase challengeBossUseCase;
    private final GetBossHistoryUseCase getBossHistoryUseCase;
    private final GetBossCooldownsUseCase getBossCooldownsUseCase;

    @GetMapping("/available")
    public ResponseEntity<List<BossDefinitionResponse>> getAvailable(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(getAvailableBossesUseCase.execute(authorization));
    }

    @PostMapping("/{bossCode}/challenge")
    public ResponseEntity<BossChallengeResponse> challenge(@RequestHeader("Authorization") String authorization, @PathVariable String bossCode, @RequestBody @Valid ChallengeBossRequest request) {
        return ResponseEntity.ok(challengeBossUseCase.execute(authorization, bossCode, request.digimonId()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<BossAttemptResponse>> getHistory(@RequestHeader("Authorization") String authorization, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getBossHistoryUseCase.execute(authorization, page, size));
    }

    @GetMapping("/cooldowns")
    public ResponseEntity<List<BossCooldownResponse>> getCooldowns(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(getBossCooldownsUseCase.execute(authorization));
    }

    public BossController(final GetAvailableBossesUseCase getAvailableBossesUseCase, final ChallengeBossUseCase challengeBossUseCase, final GetBossHistoryUseCase getBossHistoryUseCase, final GetBossCooldownsUseCase getBossCooldownsUseCase) {
        this.getAvailableBossesUseCase = getAvailableBossesUseCase;
        this.challengeBossUseCase = challengeBossUseCase;
        this.getBossHistoryUseCase = getBossHistoryUseCase;
        this.getBossCooldownsUseCase = getBossCooldownsUseCase;
    }
}
