package com.dro.modules.ranking.api;

import com.dro.modules.ranking.application.TrainerRankingService;
import com.dro.modules.ranking.api.dto.response.TrainerRankingDtos.Page;
import com.dro.modules.ranking.api.dto.response.TrainerRankingDtos.Profile;
import java.util.UUID;
import com.dro.shared.util.TokenExtractor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ranking/players")
public class TrainerRankingController {
    private final TrainerRankingService service;
    public TrainerRankingController(TrainerRankingService service) { this.service = service; }
    @GetMapping
    public Page page(@RequestHeader("Authorization") String token,
                     @RequestParam(defaultValue = "0") int page,
                     @RequestParam(defaultValue = "10") int size,
                     @RequestParam(required = false) String search) {
        return service.page(TokenExtractor.extractPlayerId(token), page, size, search);
    }
    @GetMapping("/{playerId}")
    public Profile profile(@RequestHeader("Authorization") String token, @PathVariable UUID playerId) {
        TokenExtractor.extractPlayerId(token);
        return service.profile(playerId);
    }
}
