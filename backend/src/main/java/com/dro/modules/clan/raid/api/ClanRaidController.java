package com.dro.modules.clan.raid.api;

import com.dro.modules.clan.raid.api.dto.response.AttackClanRaidResponse;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidResponse;
import com.dro.modules.clan.raid.application.AttackClanRaidUseCase;
import com.dro.modules.clan.raid.application.GetClanRaidUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Componente da camada de controller da API do módulo de Clãs.
 */
@RestController
@RequestMapping("/clan-raids")
public class ClanRaidController {
    private final GetClanRaidUseCase getClanRaidUseCase;
    private final AttackClanRaidUseCase attackClanRaidUseCase;

    @GetMapping("/me")
    public ResponseEntity<ClanRaidResponse> getMyClanRaid(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(getClanRaidUseCase.execute(authorization));
    }

    @PostMapping("/attack")
    public ResponseEntity<AttackClanRaidResponse> attack(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(attackClanRaidUseCase.execute(authorization));
    }

    public ClanRaidController(final GetClanRaidUseCase getClanRaidUseCase, final AttackClanRaidUseCase attackClanRaidUseCase) {
        this.getClanRaidUseCase = getClanRaidUseCase;
        this.attackClanRaidUseCase = attackClanRaidUseCase;
    }
}
