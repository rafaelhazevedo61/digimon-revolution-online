package com.dro.modules.equipment.api;

import com.dro.modules.equipment.api.dto.request.EquipRequest;
import com.dro.modules.equipment.api.dto.request.GrantEquipmentRequest;
import com.dro.modules.equipment.api.dto.request.RefineEquipmentRequest;
import com.dro.modules.equipment.api.dto.request.UnequipRequest;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.api.dto.response.GrantEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.RefineEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.RefinePreviewResponse;
import com.dro.modules.equipment.application.*;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final GetDigimonInventoryUseCase getDigimonInventoryUseCase;
    private final GetDigimonEquipmentUseCase getDigimonEquipmentUseCase;
    private final EquipUseCase equipUseCase;
    private final UnequipUseCase unequipUseCase;
    private final UnequipAllUseCase unequipAllUseCase;
    private final GrantEquipmentUseCase grantEquipmentUseCase;
    private final RefineEquipmentUseCase refineEquipmentUseCase;
    private final com.dro.modules.equipment.infra.EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final com.dro.modules.player.infra.PlayerRepository playerRepository;
    private final com.dro.modules.digimon.infra.DigimonRepository digimonRepository;

    @GetMapping("/digimon/{digimonId}/inventory")
    public ResponseEntity<List<EquipmentResponse>> getDigimonInventory(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID digimonId
    ) {
        return ResponseEntity.ok(getDigimonInventoryUseCase.execute(authorization, digimonId));
    }

    @GetMapping("/digimon/{digimonId}")
    public ResponseEntity<DigimonEquipmentResponse> getDigimonEquipment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID digimonId
    ) {
        return ResponseEntity.ok(getDigimonEquipmentUseCase.execute(authorization, digimonId));
    }

    @PostMapping("/equip")
    public ResponseEntity<Map<String, String>> equip(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid EquipRequest request
    ) {
        equipUseCase.execute(authorization, request.equipmentId());
        return ResponseEntity.ok(Map.of("message", "Equipment equipped successfully"));
    }

    @PostMapping("/unequip")
    public ResponseEntity<Map<String, String>> unequip(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid UnequipRequest request
    ) {
        unequipUseCase.execute(authorization, request.equipmentId());
        return ResponseEntity.ok(Map.of("message", "Equipment unequipped successfully"));
    }

    @PostMapping("/unequip-all")
    public ResponseEntity<Map<String, Object>> unequipAll(
            @RequestHeader("Authorization") String authorization
    ) {
        int count = unequipAllUseCase.execute(authorization);
        return ResponseEntity.ok(Map.of("message", count + " equipment(s) unequipped successfully", "count", count));
    }

    @PostMapping("/grant")
    public ResponseEntity<GrantEquipmentResponse> grant(
            @RequestBody @Valid GrantEquipmentRequest request
    ) {
        UUID equipmentId = grantEquipmentUseCase.execute(
                request.digimonId(), request.templateName(), request.rarity()
        );
        return ResponseEntity.ok(new GrantEquipmentResponse(
                equipmentId, "Equipment granted successfully"
        ));
    }

    @PostMapping("/refine")
    public ResponseEntity<RefineEquipmentResponse> refine(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid RefineEquipmentRequest request
    ) {
        return ResponseEntity.ok(refineEquipmentUseCase.execute(authorization, request.equipmentId()));
    }

    @GetMapping("/{equipmentId}/refine-preview")
    public ResponseEntity<RefinePreviewResponse> refinePreview(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID equipmentId
    ) {
        UUID playerId = com.dro.shared.util.TokenExtractor.extractPlayerId(authorization);
        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new com.dro.shared.exception.NotFoundException("Player not found"));
        var digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new com.dro.shared.exception.NotFoundException("Active digimon not found"));

        var equip = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new com.dro.shared.exception.NotFoundException("Equipment not found"));

        int currentLevel = equip.getRefinementLevel();
        int costBits = EquipmentRules.refinementCostBits(currentLevel);
        int currentStones = inventoryRepository
                .findByDigimonIdAndItemType(digimon.getId(), ItemType.REFINEMENT_STONE)
                .map(i -> i.getQuantity())
                .orElse(0);
        boolean canRefine = currentLevel < EquipmentRules.MAX_REFINEMENT_LEVEL
                && digimon.getBits() >= costBits
                && currentStones >= 1;

        return ResponseEntity.ok(new RefinePreviewResponse(
                currentLevel,
                currentLevel + 1,
                costBits,
                1,
                digimon.getBits(),
                currentStones,
                canRefine
        ));
    }
}
