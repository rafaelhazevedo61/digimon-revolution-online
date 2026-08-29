package com.dro.modules.arena.api;

import com.dro.modules.arena.api.dto.request.BuyArenaShopRequest;
import com.dro.modules.arena.api.dto.request.ChallengeArenaRequest;
import com.dro.modules.arena.api.dto.response.ArenaHistoryEntryResponse;
import com.dro.modules.arena.api.dto.response.ArenaLobbyResponse;
import com.dro.modules.arena.api.dto.response.ArenaMatchResponse;
import com.dro.modules.arena.api.dto.response.ArenaRankingEntryResponse;
import com.dro.modules.arena.api.dto.response.ArenaSeasonRankingEntryResponse;
import com.dro.modules.arena.api.dto.response.ArenaShopResponse;
import com.dro.modules.arena.api.dto.response.PlayerArenaStatisticsResponse;
import com.dro.modules.arena.api.dto.response.BuyArenaShopResponse;
import com.dro.modules.arena.application.BuyArenaShopProductUseCase;
import com.dro.modules.arena.application.ChallengeArenaUseCase;
import com.dro.modules.arena.application.GetArenaHistoryUseCase;
import com.dro.modules.arena.application.GetArenaLobbyUseCase;
import com.dro.modules.arena.application.GetArenaRankingUseCase;
import com.dro.modules.arena.application.GetArenaSeasonRankingUseCase;
import com.dro.modules.arena.application.GetArenaShopUseCase;
import com.dro.modules.arena.application.PlayerArenaStatisticsService;
import com.dro.shared.util.TokenExtractor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Componente da camada de controller da API do módulo de Arena.
 */
@RestController
@RequestMapping("/arena")
public class ArenaController {
    private final GetArenaLobbyUseCase getArenaLobbyUseCase;
    private final ChallengeArenaUseCase challengeArenaUseCase;
    private final GetArenaRankingUseCase getArenaRankingUseCase;
    private final GetArenaSeasonRankingUseCase getArenaSeasonRankingUseCase;
    private final GetArenaHistoryUseCase getArenaHistoryUseCase;
    private final GetArenaShopUseCase getArenaShopUseCase;
    private final BuyArenaShopProductUseCase buyArenaShopProductUseCase;
    private final PlayerArenaStatisticsService playerArenaStatisticsService;

    @GetMapping("/lobby")
    public ResponseEntity<ArenaLobbyResponse> getLobby(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(getArenaLobbyUseCase.execute(authorization));
    }

    @PostMapping("/challenge")
    public ResponseEntity<ArenaMatchResponse> challenge(@RequestHeader("Authorization") String authorization, @RequestBody @Valid ChallengeArenaRequest request) {
        return ResponseEntity.ok(challengeArenaUseCase.execute(authorization, request.opponentDigimonId()));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ArenaRankingEntryResponse>> getRanking(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getArenaRankingUseCase.execute(page, size));
    }

    @GetMapping("/season-ranking")
    public ResponseEntity<List<ArenaSeasonRankingEntryResponse>> getSeasonRanking(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getArenaSeasonRankingUseCase.execute(page, size));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ArenaHistoryEntryResponse>> getHistory(@RequestHeader("Authorization") String authorization, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getArenaHistoryUseCase.execute(authorization, page, size));
    }

    @GetMapping("/statistics")
    public ResponseEntity<PlayerArenaStatisticsResponse> getStatistics(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(PlayerArenaStatisticsResponse.from(playerArenaStatisticsService.get(TokenExtractor.extractPlayerId(authorization))));
    }

    @GetMapping("/shop")
    public ResponseEntity<ArenaShopResponse> getShop(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(getArenaShopUseCase.execute(authorization));
    }

    @PostMapping("/shop/buy")
    public ResponseEntity<BuyArenaShopResponse> buyFromShop(@RequestHeader("Authorization") String authorization, @RequestBody @Valid BuyArenaShopRequest request) {
        return ResponseEntity.ok(buyArenaShopProductUseCase.execute(authorization, request));
    }

    public ArenaController(final GetArenaLobbyUseCase getArenaLobbyUseCase, final ChallengeArenaUseCase challengeArenaUseCase, final GetArenaRankingUseCase getArenaRankingUseCase, final GetArenaSeasonRankingUseCase getArenaSeasonRankingUseCase, final GetArenaHistoryUseCase getArenaHistoryUseCase, final GetArenaShopUseCase getArenaShopUseCase, final BuyArenaShopProductUseCase buyArenaShopProductUseCase, final PlayerArenaStatisticsService playerArenaStatisticsService) {
        this.getArenaLobbyUseCase = getArenaLobbyUseCase;
        this.challengeArenaUseCase = challengeArenaUseCase;
        this.getArenaRankingUseCase = getArenaRankingUseCase;
        this.getArenaSeasonRankingUseCase = getArenaSeasonRankingUseCase;
        this.getArenaHistoryUseCase = getArenaHistoryUseCase;
        this.getArenaShopUseCase = getArenaShopUseCase;
        this.buyArenaShopProductUseCase = buyArenaShopProductUseCase;
        this.playerArenaStatisticsService = playerArenaStatisticsService;
    }
}
