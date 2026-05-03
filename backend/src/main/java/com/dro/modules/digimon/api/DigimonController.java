package com.dro.modules.digimon.api;

import com.dro.modules.digimon.api.dto.request.EvolveDigimonRequest;
import com.dro.modules.digimon.api.dto.request.RebirthDigimonRequest;
import com.dro.modules.digimon.api.dto.request.RenameDigimonRequest;
import com.dro.modules.digimon.api.dto.response.DigimonLineageResponse;
import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.api.dto.request.SelectDigimonRequest;
import com.dro.modules.digimon.api.dto.response.RebirthPreviewResponse;
import com.dro.modules.digimon.api.dto.response.TraitHatchSimulationResponse;
import com.dro.modules.digimon.application.*;
import com.dro.modules.digimon.domain.DigimonLevelRules;
import com.dro.modules.evolution.api.dto.response.EvolutionOptionsResponse;
import com.dro.modules.evolution.application.GetEvolutionOptionsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/digimon")
@RequiredArgsConstructor
public class DigimonController {

    private final GetDigimonUseCase useCase;
    private final GetDigimonByIdUseCase getDigimonByIdUseCase;
    private final AddExperienceUseCase addExperienceUseCase;
    private final SelectActiveDigimonUseCase selectUseCase;
    private final EvolveDigimonUseCase evolveDigimonUseCase;
    private final RebirthUseCase rebirthUseCase;
    private final GetDigimonLineageUseCase getDigimonLineageUseCase;
    private final RebirthPreviewUseCase rebirthPreviewUseCase;
    private final SimulateTraitHatchUseCase simulateTraitHatchUseCase;
    private final RenameDigimonUseCase renameDigimonUseCase;
    private final GetEvolutionOptionsUseCase getEvolutionOptionsUseCase;

    @GetMapping("/me")
    public ResponseEntity<List<DigimonResponse>> me(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(useCase.execute(authorization));
    }

    @PostMapping("/add-xp")
    public ResponseEntity<Void> addXp(
            @RequestHeader("Authorization") String authorization,
            @RequestParam int amount
    ) {
        addExperienceUseCase.execute(authorization, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/select")
    public ResponseEntity<Void> select(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SelectDigimonRequest request
    ) {
        selectUseCase.execute(authorization, request.digimonId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/evolve")
    public ResponseEntity<String> evolve(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) EvolveDigimonRequest request
    ) {
        Long evolutionLineId = request != null ? request.evolutionLineId() : null;
        evolveDigimonUseCase.execute(authorization, evolutionLineId);
        return ResponseEntity.ok("Digimon evolved successfully");
    }

    @GetMapping("/{digimonId}/evolution-options")
    public ResponseEntity<EvolutionOptionsResponse> evolutionOptions(
            @PathVariable UUID digimonId
    ) {
        return ResponseEntity.ok(getEvolutionOptionsUseCase.execute(digimonId));
    }

    @PostMapping("/rebirth")
    public ResponseEntity<String> rebirth(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid RebirthDigimonRequest request
    ) {
        rebirthUseCase.execute(authorization, request.digimonId());
        return ResponseEntity.ok("Digimon reborn successfully");
    }

    @GetMapping("/{digimonId}")
    public ResponseEntity<DigimonResponse> getById(
            @PathVariable UUID digimonId
    ) {
        return ResponseEntity.ok(getDigimonByIdUseCase.execute(digimonId));
    }

    @GetMapping("/level-table")
    public ResponseEntity<List<DigimonLevelRules.LevelExperienceData>> levelTable() {
        return ResponseEntity.ok(DigimonLevelRules.getExperienceTable());
    }

    @GetMapping("/{digimonId}/lineage")
    public ResponseEntity<DigimonLineageResponse> lineage(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID digimonId
    ) {
        return ResponseEntity.ok(
                getDigimonLineageUseCase.execute(authorization, digimonId)
        );
    }

    @GetMapping("/{digimonId}/rebirth-preview")
    public ResponseEntity<RebirthPreviewResponse> rebirthPreview(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID digimonId
    ) {
        return ResponseEntity.ok(
                rebirthPreviewUseCase.execute(authorization, digimonId)
        );
    }

    @GetMapping("/simulator/trait-hatch")
    public ResponseEntity<TraitHatchSimulationResponse> simulateTraitHatch(
            @RequestParam(defaultValue = "1000") int attempts
    ) {
        return ResponseEntity.ok(
                simulateTraitHatchUseCase.execute(attempts)
        );
    }

    @PutMapping("/rename")
    public ResponseEntity<String> rename(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid RenameDigimonRequest request
    ) {
        renameDigimonUseCase.execute(authorization, request.digimonId(), request.newName());
        return ResponseEntity.ok("Digimon renamed successfully");
    }
}
