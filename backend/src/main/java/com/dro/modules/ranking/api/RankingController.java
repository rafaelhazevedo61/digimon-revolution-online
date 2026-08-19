package com.dro.modules.ranking.api;

import com.dro.modules.ranking.api.dto.response.RankingEntryResponse;
import com.dro.modules.ranking.application.GetRankingUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Componente da camada de controller da API do módulo de Ranking.
 */
@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final GetRankingUseCase getRankingUseCase;

    @GetMapping("/level")
    public ResponseEntity<List<RankingEntryResponse>> byLevel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getRankingUseCase.byLevel(page, size));
    }

    @GetMapping("/grade")
    public ResponseEntity<List<RankingEntryResponse>> byGrade(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getRankingUseCase.byGrade(page, size));
    }

    @GetMapping("/rebirth")
    public ResponseEntity<List<RankingEntryResponse>> byRebirth(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getRankingUseCase.byRebirth(page, size));
    }
}
