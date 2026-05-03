package com.dro.modules.evolution.api;

import com.dro.modules.evolution.api.dto.response.AvailableEvolutionLineResponse;
import com.dro.modules.evolution.application.GetAvailableEvolutionLinesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evolution-lines")
@RequiredArgsConstructor
public class EvolutionLineController {

    private final GetAvailableEvolutionLinesUseCase getAvailableEvolutionLinesUseCase;

    @GetMapping("/available")
    public ResponseEntity<List<AvailableEvolutionLineResponse>> getAvailableEvolutionLines() {
        return ResponseEntity.ok(getAvailableEvolutionLinesUseCase.execute());
    }
}