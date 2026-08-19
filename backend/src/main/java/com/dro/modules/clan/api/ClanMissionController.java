package com.dro.modules.clan.api;

import com.dro.modules.clan.api.dto.response.ClanHonorMarksRankingEntryResponse;
import com.dro.modules.clan.api.dto.response.ClanMissionResponse;
import com.dro.modules.clan.api.dto.response.PlayerClanMissionResponse;
import com.dro.modules.clan.application.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Clãs.
 */
@RestController
@RequestMapping("/clan-missions")
@RequiredArgsConstructor
public class ClanMissionController {

    private final ListClanMissionsUseCase listClanMissionsUseCase;
    private final AcceptClanMissionUseCase acceptClanMissionUseCase;
    private final ClaimClanMissionUseCase claimClanMissionUseCase;
    private final GetMyClanMissionUseCase getMyClanMissionUseCase;
    private final GetClanHonorMarksRankingUseCase getClanHonorMarksRankingUseCase;

    @GetMapping
    public ResponseEntity<List<ClanMissionResponse>> list(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(listClanMissionsUseCase.execute(authorization));
    }

    @GetMapping("/me")
    public ResponseEntity<PlayerClanMissionResponse> getMyMission(
            @RequestHeader("Authorization") String authorization
    ) {
        PlayerClanMissionResponse response = getMyClanMissionUseCase.execute(authorization);
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<PlayerClanMissionResponse> accept(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(acceptClanMissionUseCase.execute(authorization, id));
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<PlayerClanMissionResponse> claim(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(claimClanMissionUseCase.execute(authorization, id));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ClanHonorMarksRankingEntryResponse>> getRanking(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(getClanHonorMarksRankingUseCase.execute(authorization));
    }
}
