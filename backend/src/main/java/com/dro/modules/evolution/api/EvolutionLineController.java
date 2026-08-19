package com.dro.modules.evolution.api;

import com.dro.modules.evolution.api.dto.response.AvailableEvolutionLineResponse;
import com.dro.modules.evolution.api.dto.response.EvolutionLinePageResponse;
import com.dro.modules.evolution.application.GetAvailableEvolutionLinesUseCase;
import com.dro.modules.evolution.application.GetEvolutionLinesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Componente da camada de controller da API do módulo de Evolução.
 */
@RestController
@RequestMapping("/evolution-lines")
@RequiredArgsConstructor
public class EvolutionLineController {

    private final GetEvolutionLinesUseCase getEvolutionLinesUseCase;
    private final GetAvailableEvolutionLinesUseCase getAvailableEvolutionLinesUseCase;

    @GetMapping("/available")
    public ResponseEntity<List<AvailableEvolutionLineResponse>> getAvailableEvolutionLines() {
        return ResponseEntity.ok(getAvailableEvolutionLinesUseCase.execute());
    }

    @GetMapping
    public ResponseEntity<EvolutionLinePageResponse> getEvolutionLines(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                normalizePageSize(size),
                Sort.by(
                        Sort.Order.asc("code")
                )
        );

        return ResponseEntity.ok(
                getEvolutionLinesUseCase.execute(
                        code,
                        name,
                        active,
                        pageRequest
                )
        );
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }
}