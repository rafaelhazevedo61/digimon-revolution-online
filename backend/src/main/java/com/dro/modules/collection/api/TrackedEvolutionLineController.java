package com.dro.modules.collection.api;

import com.dro.modules.collection.application.TrackedEvolutionLineService;
import com.dro.shared.util.TokenExtractor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collection/tracked-evolution-line")
public class TrackedEvolutionLineController {
    private final TrackedEvolutionLineService service;

    public TrackedEvolutionLineController(TrackedEvolutionLineService service) {
        this.service = service;
    }

    @GetMapping
    public TrackedEvolutionLineService.Status get(@RequestHeader("Authorization") String authorization) {
        return service.get(TokenExtractor.extractPlayerId(authorization));
    }

    @PutMapping
    public TrackedEvolutionLineService.Status update(@RequestHeader("Authorization") String authorization,
                                                    @RequestBody @Valid Selection request) {
        return service.update(TokenExtractor.extractPlayerId(authorization), request.lineCode());
    }

    @DeleteMapping
    public TrackedEvolutionLineService.Status clear(@RequestHeader("Authorization") String authorization) {
        return service.update(TokenExtractor.extractPlayerId(authorization), null);
    }

    public record Selection(@NotBlank String lineCode) {}
}
