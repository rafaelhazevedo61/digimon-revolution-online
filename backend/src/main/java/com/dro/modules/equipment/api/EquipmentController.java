package com.dro.modules.equipment.api;

import com.dro.modules.equipment.api.dto.request.EquipRequest;
import com.dro.modules.equipment.api.dto.request.RefineEquipmentRequest;
import com.dro.modules.equipment.api.dto.request.UnequipRequest;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentPageResponse;
import com.dro.modules.equipment.api.dto.response.RefineEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.RefinePreviewResponse;
import com.dro.modules.equipment.application.*;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Equipamentos.
 */
@RestController
@RequestMapping("/equipment")
public class EquipmentController {
    private final GetDigimonInventoryUseCase getDigimonInventoryUseCase;
    private final GetDigimonEquipmentUseCase getDigimonEquipmentUseCase;
    private final EquipUseCase equipUseCase;
    private final UnequipUseCase unequipUseCase;
    private final UnequipAllUseCase unequipAllUseCase;
    private final RefineEquipmentUseCase refineEquipmentUseCase;
    private final com.dro.modules.equipment.infra.EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final com.dro.modules.player.infra.PlayerRepository playerRepository;
    private final com.dro.modules.digimon.infra.DigimonRepository digimonRepository;

    @GetMapping("/inventory")
    public ResponseEntity<List<EquipmentResponse>> getInventory(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(getDigimonInventoryUseCase.execute(authorization));
    }

    @GetMapping("/inventory/page")
    public ResponseEntity<EquipmentPageResponse> getInventoryPage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "ALL") String slot,
            @RequestParam(defaultValue = "ALL") String rarity,
            @RequestParam(defaultValue = "name-asc") String sort) {
        UUID playerId = com.dro.shared.util.TokenExtractor.extractPlayerId(authorization);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);
        String query = search == null ? "" : search.toLowerCase(Locale.ROOT).trim();
        List<EquipmentResponse> filtered = equipmentRepository.findByPlayerId(playerId).stream()
                .map(EquipmentResponse::from)
                .filter(equipment -> (query.isEmpty() || String.join(" ", String.valueOf(equipment.name()), String.valueOf(equipment.setCode()), String.valueOf(equipment.slot()), String.valueOf(equipment.rarity()), String.valueOf(equipment.tier()), String.valueOf(equipment.refinementLevel())).toLowerCase(Locale.ROOT).contains(query)))
                .filter(equipment -> "ALL".equalsIgnoreCase(slot) || String.valueOf(equipment.slot()).equalsIgnoreCase(slot))
                .filter(equipment -> "ALL".equalsIgnoreCase(rarity) || String.valueOf(equipment.rarity()).equalsIgnoreCase(rarity))
                .sorted(equipmentComparator(sort))
                .toList();
        int from = Math.min(safePage * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        return ResponseEntity.ok(EquipmentPageResponse.of(filtered.subList(from, to), safePage, safeSize, filtered.size()));
    }

    private java.util.Comparator<EquipmentResponse> equipmentComparator(String sort) {
        java.util.Comparator<EquipmentResponse> equippedFirst = java.util.Comparator.comparing(EquipmentResponse::equipped).reversed();
        java.util.Comparator<EquipmentResponse> byName = java.util.Comparator.comparing(EquipmentResponse::name, String.CASE_INSENSITIVE_ORDER).thenComparing(EquipmentResponse::id);
        return switch (sort == null ? "name-asc" : sort) {
            case "name-desc" -> equippedFirst.thenComparing(byName.reversed());
            case "rarity-desc" -> equippedFirst.thenComparing(Comparator.comparingInt((EquipmentResponse e) -> rarityRank(e.rarity())).reversed()).thenComparing(byName);
            case "rarity-asc" -> equippedFirst.thenComparingInt(e -> rarityRank(e.rarity())).thenComparing(byName);
            case "level-desc" -> equippedFirst.thenComparing(Comparator.comparingInt(EquipmentResponse::tier).reversed()).thenComparing(byName);
            case "refinement-desc" -> equippedFirst.thenComparing(Comparator.comparingInt(EquipmentResponse::refinementLevel).reversed()).thenComparing(byName);
            default -> equippedFirst.thenComparing(byName);
        };
    }

    private int rarityRank(Object rarity) {
        return switch (String.valueOf(rarity).toUpperCase(Locale.ROOT)) {
            case "COMMON" -> 1; case "RARE" -> 2; case "EPIC" -> 3; case "LEGENDARY" -> 4; default -> 0;
        };
    }

    /** Alias de compatibilidade; o inventário agora é global do jogador. */
    @GetMapping("/digimon/{digimonId}/inventory")
    public ResponseEntity<List<EquipmentResponse>> getDigimonInventory(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        return ResponseEntity.ok(getDigimonInventoryUseCase.execute(authorization));
    }

    @GetMapping("/digimon/{digimonId}")
    public ResponseEntity<DigimonEquipmentResponse> getDigimonEquipment(@RequestHeader("Authorization") String authorization, @PathVariable UUID digimonId) {
        return ResponseEntity.ok(getDigimonEquipmentUseCase.execute(authorization, digimonId));
    }

    @PostMapping("/equip")
    public ResponseEntity<Map<String, String>> equip(@RequestHeader("Authorization") String authorization, @RequestBody @Valid EquipRequest request) {
        equipUseCase.execute(authorization, request.equipmentId());
        return ResponseEntity.ok(Map.of("message", "Equipment equipped successfully"));
    }

    @PostMapping("/unequip")
    public ResponseEntity<Map<String, String>> unequip(@RequestHeader("Authorization") String authorization, @RequestBody @Valid UnequipRequest request) {
        unequipUseCase.execute(authorization, request.equipmentId());
        return ResponseEntity.ok(Map.of("message", "Equipment unequipped successfully"));
    }

    @PostMapping("/unequip-all")
    public ResponseEntity<Map<String, Object>> unequipAll(@RequestHeader("Authorization") String authorization) {
        int count = unequipAllUseCase.execute(authorization);
        return ResponseEntity.ok(Map.of("message", count + " equipment(s) unequipped successfully", "count", count));
    }

    @PostMapping("/refine")
    public ResponseEntity<RefineEquipmentResponse> refine(@RequestHeader("Authorization") String authorization, @RequestBody @Valid RefineEquipmentRequest request) {
        return ResponseEntity.ok(refineEquipmentUseCase.execute(authorization, request.equipmentId()));
    }

    @GetMapping("/{equipmentId}/refine-preview")
    public ResponseEntity<RefinePreviewResponse> refinePreview(@RequestHeader("Authorization") String authorization, @PathVariable UUID equipmentId) {
        UUID playerId = com.dro.shared.util.TokenExtractor.extractPlayerId(authorization);
        var player = playerRepository.findById(playerId).orElseThrow(() -> new com.dro.shared.exception.NotFoundException("Player not found"));
        var digimon = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new com.dro.shared.exception.NotFoundException("Active digimon not found"));
        var equip = equipmentRepository.findById(equipmentId).orElseThrow(() -> new com.dro.shared.exception.NotFoundException("Equipment not found"));
        if (!equip.getDigimonId().equals(digimon.getId())) {
            throw new com.dro.shared.exception.ForbiddenException("Equipment does not belong to this Digimon");
        }
        int currentLevel = equip.getRefinementLevel();
        int costBits = EquipmentRules.refinementCostBits(currentLevel);
        int currentStones = inventoryRepository.findByDigimonIdAndItemType(digimon.getId(), ItemType.REFINEMENT_STONE).map(i -> i.getQuantity()).orElse(0);
        boolean canRefine = currentLevel < EquipmentRules.MAX_REFINEMENT_LEVEL && digimon.getBits() >= costBits && currentStones >= 1;
        int successRate = EquipmentRules.refinementSuccessRate(currentLevel);
        return ResponseEntity.ok(new RefinePreviewResponse(currentLevel, currentLevel + 1, successRate, costBits, 1, digimon.getBits(), currentStones, canRefine));
    }

    public EquipmentController(final GetDigimonInventoryUseCase getDigimonInventoryUseCase, final GetDigimonEquipmentUseCase getDigimonEquipmentUseCase, final EquipUseCase equipUseCase, final UnequipUseCase unequipUseCase, final UnequipAllUseCase unequipAllUseCase, final RefineEquipmentUseCase refineEquipmentUseCase, final com.dro.modules.equipment.infra.EquipmentRepository equipmentRepository, final InventoryRepository inventoryRepository, final com.dro.modules.player.infra.PlayerRepository playerRepository, final com.dro.modules.digimon.infra.DigimonRepository digimonRepository) {
        this.getDigimonInventoryUseCase = getDigimonInventoryUseCase;
        this.getDigimonEquipmentUseCase = getDigimonEquipmentUseCase;
        this.equipUseCase = equipUseCase;
        this.unequipUseCase = unequipUseCase;
        this.unequipAllUseCase = unequipAllUseCase;
        this.refineEquipmentUseCase = refineEquipmentUseCase;
        this.equipmentRepository = equipmentRepository;
        this.inventoryRepository = inventoryRepository;
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
    }
}
