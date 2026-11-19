package com.dro.modules.digimon.api;

import com.dro.modules.digimon.api.dto.request.BulkSacrificeDigimonRequest;
import com.dro.modules.digimon.api.dto.request.EvolveDigimonRequest;
import com.dro.modules.digimon.api.dto.request.RebirthDigimonRequest;
import com.dro.modules.digimon.api.dto.request.RenameDigimonRequest;
import com.dro.modules.digimon.api.dto.response.BulkSacrificeDigimonResponse;
import com.dro.modules.digimon.api.dto.response.DigimonLineageResponse;
import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.api.dto.request.SelectDigimonRequest;
import com.dro.modules.digimon.api.dto.response.RebirthPreviewResponse;
import com.dro.modules.digimon.application.*;
import com.dro.modules.digimon.domain.DigimonLevelRules;
import com.dro.modules.evolution.api.dto.response.EvolutionOptionsResponse;
import com.dro.modules.evolution.application.GetEvolutionOptionsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Digimon.
 */
@RestController
@RequestMapping("/digimon")
public class DigimonController {
    private final GetDigimonUseCase useCase;
    private final GetDigimonByIdUseCase getDigimonByIdUseCase;
    private final SelectActiveDigimonUseCase selectUseCase;
    private final EvolveDigimonUseCase evolveDigimonUseCase;
    private final RebirthUseCase rebirthUseCase;
    private final GetDigimonLineageUseCase getDigimonLineageUseCase;
    private final RebirthPreviewUseCase rebirthPreviewUseCase;
    private final RenameDigimonUseCase renameDigimonUseCase;
    private final StoreDigimonUseCase storeDigimonUseCase;
    private final RetrieveDigimonUseCase retrieveDigimonUseCase;
    private final GetEvolutionOptionsUseCase getEvolutionOptionsUseCase;
    private final SacrificeDigimonUseCase sacrificeDigimonUseCase;
    private final BulkSacrificeDigimonUseCase bulkSacrificeDigimonUseCase;
    private final ToggleDigimonLockUseCase toggleDigimonLockUseCase;

    @GetMapping("/me")
    public ResponseEntity<List<DigimonResponse>> me(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(useCase.execute(authorization));
    }

    @PostMapping("/select")
    public ResponseEntity<Void> select(@RequestHeader("Authorization") String authorization, @RequestBody @Valid SelectDigimonRequest request) {
        selectUseCase.execute(authorization, request.digimonId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/evolve")
    public ResponseEntity<Map<String, String>> evolve(@RequestHeader("Authorization") String authorization, @RequestBody(required = false) EvolveDigimonRequest request) {
        Long evolutionLineId = request != null ? request.evolutionLineId() : null;
        evolveDigimonUseCase.execute(authorization, evolutionLineId);
        return ResponseEntity.ok(Map.of("message", "Digimon evolved successfully"));
    }

    @GetMapping("/{digimonId}/evolution-options")
    public ResponseEntity<EvolutionOptionsResponse> evolutionOptions(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        return ResponseEntity.ok(getEvolutionOptionsUseCase.execute(authorization, digimonId));
    }

    @PostMapping("/rebirth")
    public ResponseEntity<Map<String, String>> rebirth(@RequestHeader("Authorization") String authorization, @RequestBody @Valid RebirthDigimonRequest request) {
        rebirthUseCase.execute(authorization, request.digimonId(), request.codeInfiniteHpOrZero(), request.codeInfiniteAttackOrZero(), request.codeInfiniteDefenseOrZero(), request.preserveRarityOrFalse());
        return ResponseEntity.ok(Map.of("message", "Digimon reborn successfully"));
    }

    @GetMapping("/{digimonId}")
    public ResponseEntity<DigimonResponse> getById(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        return ResponseEntity.ok(getDigimonByIdUseCase.execute(authorization, digimonId));
    }

    @GetMapping("/level-table")
    public ResponseEntity<List<DigimonLevelRules.LevelExperienceData>> levelTable() {
        return ResponseEntity.ok(DigimonLevelRules.getExperienceTable());
    }

    @GetMapping("/{digimonId}/lineage")
    public ResponseEntity<DigimonLineageResponse> lineage(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        return ResponseEntity.ok(getDigimonLineageUseCase.execute(authorization, digimonId));
    }

    @GetMapping("/{digimonId}/rebirth-preview")
    public ResponseEntity<RebirthPreviewResponse> rebirthPreview(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID digimonId,
            @RequestParam(defaultValue = "false") boolean preserveRarity
    ) {
        return ResponseEntity.ok(rebirthPreviewUseCase.execute(authorization, digimonId, preserveRarity));
    }

    @PutMapping("/rename")
    public ResponseEntity<Map<String, String>> rename(@RequestHeader("Authorization") String authorization, @RequestBody @Valid RenameDigimonRequest request) {
        renameDigimonUseCase.execute(authorization, request.digimonId(), request.newName());
        return ResponseEntity.ok(Map.of("message", "Digimon renamed successfully"));
    }

    @PostMapping("/{digimonId}/store")
    public ResponseEntity<DigimonResponse> store(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        var digimon = storeDigimonUseCase.execute(authorization, digimonId);
        return ResponseEntity.ok(DigimonResponse.from(digimon));
    }

    @PostMapping("/{digimonId}/sacrifice")
    public ResponseEntity<Map<String, Object>> sacrifice(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        int reward = sacrificeDigimonUseCase.execute(authorization, digimonId);
        return ResponseEntity.ok(Map.of("message", "Digimon sacrificed successfully", "digitalDataReceived", reward));
    }

    @PostMapping("/sacrifice/bulk")
    public ResponseEntity<BulkSacrificeDigimonResponse> bulkSacrifice(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid BulkSacrificeDigimonRequest request
    ) {
        return ResponseEntity.ok(bulkSacrificeDigimonUseCase.execute(authorization, request.digimonIds()));
    }

    @PatchMapping("/{digimonId}/lock")
    public ResponseEntity<DigimonResponse> toggleLock(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        return ResponseEntity.ok(DigimonResponse.from(toggleDigimonLockUseCase.execute(authorization, digimonId)));
    }

    @PostMapping("/{digimonId}/retrieve")
    public ResponseEntity<DigimonResponse> retrieve(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        var digimon = retrieveDigimonUseCase.execute(authorization, digimonId);
        return ResponseEntity.ok(DigimonResponse.from(digimon));
    }

    @GetMapping("/storage")
    public ResponseEntity<List<DigimonResponse>> storage(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(useCase.executeStorage(authorization));
    }

    public DigimonController(final GetDigimonUseCase useCase, final GetDigimonByIdUseCase getDigimonByIdUseCase, final SelectActiveDigimonUseCase selectUseCase, final EvolveDigimonUseCase evolveDigimonUseCase, final RebirthUseCase rebirthUseCase, final GetDigimonLineageUseCase getDigimonLineageUseCase, final RebirthPreviewUseCase rebirthPreviewUseCase, final RenameDigimonUseCase renameDigimonUseCase, final StoreDigimonUseCase storeDigimonUseCase, final RetrieveDigimonUseCase retrieveDigimonUseCase, final GetEvolutionOptionsUseCase getEvolutionOptionsUseCase, final SacrificeDigimonUseCase sacrificeDigimonUseCase, final BulkSacrificeDigimonUseCase bulkSacrificeDigimonUseCase, final ToggleDigimonLockUseCase toggleDigimonLockUseCase) {
        this.useCase = useCase;
        this.getDigimonByIdUseCase = getDigimonByIdUseCase;
        this.selectUseCase = selectUseCase;
        this.evolveDigimonUseCase = evolveDigimonUseCase;
        this.rebirthUseCase = rebirthUseCase;
        this.getDigimonLineageUseCase = getDigimonLineageUseCase;
        this.rebirthPreviewUseCase = rebirthPreviewUseCase;
        this.renameDigimonUseCase = renameDigimonUseCase;
        this.storeDigimonUseCase = storeDigimonUseCase;
        this.retrieveDigimonUseCase = retrieveDigimonUseCase;
        this.getEvolutionOptionsUseCase = getEvolutionOptionsUseCase;
        this.sacrificeDigimonUseCase = sacrificeDigimonUseCase;
        this.bulkSacrificeDigimonUseCase = bulkSacrificeDigimonUseCase;
        this.toggleDigimonLockUseCase = toggleDigimonLockUseCase;
    }
}
