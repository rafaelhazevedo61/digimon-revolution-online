package com.dro.modules.inventory.api;

import com.dro.modules.inventory.api.dto.request.UseItemRequest;
import com.dro.modules.inventory.api.dto.response.UseItemResponse;
import com.dro.modules.inventory.api.dto.response.InventoryPageResponse;
import com.dro.modules.inventory.application.UseItemUseCase;
import com.dro.modules.digimon.api.dto.response.RarityRerollResponse;
import com.dro.modules.digimon.application.RarityRerollUseCase;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.api.dto.request.OpenChestRequest;
import com.dro.modules.loot.api.dto.response.ChestOpeningResponse;
import com.dro.modules.loot.application.OpenChestUseCase;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.shared.util.TokenExtractor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Inventário.
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryRepository repository;
    private final UseItemUseCase useItemUseCase;
    private final OpenChestUseCase openChestUseCase;
    private final RarityRerollUseCase rarityRerollUseCase;

    @GetMapping
    public ResponseEntity<?> getInventory(@RequestHeader("Authorization") String authorization) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);
        List<InventoryItem> items = repository.findByPlayerId(playerId);
        items.sort(Comparator.comparingInt(this::categoryOrder).thenComparing(this::itemName, String.CASE_INSENSITIVE_ORDER).thenComparing(this::itemCode, String.CASE_INSENSITIVE_ORDER).thenComparing(InventoryItem::getId));
        return ResponseEntity.ok(items);
    }

    @GetMapping("/page")
    public ResponseEntity<InventoryPageResponse> getInventoryPage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "ALL") String fragmentStage,
            @RequestParam(defaultValue = "ALL") String rarity,
            @RequestParam(defaultValue = "name-asc") String sort) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);
        List<InventoryItem> filtered = aggregate(repository.findByPlayerId(playerId)).stream()
                .filter(item -> item.getQuantity() > 0)
                .filter(item -> matchesItem(item, search, category, fragmentStage, rarity))
                .sorted(itemComparator(sort))
                .toList();
        int from = Math.min(safePage * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        return ResponseEntity.ok(InventoryPageResponse.of(filtered.subList(from, to), safePage, safeSize, filtered.size()));
    }

    private List<InventoryItem> aggregate(List<InventoryItem> items) {
        Map<String, InventoryItem> grouped = new LinkedHashMap<>();
        for (InventoryItem item : items) {
            String key = item.getItemDefinition() != null && item.getItemDefinition().getCode() != null
                    ? item.getItemDefinition().getCode() : String.valueOf(item.getItemType());
            InventoryItem existing = grouped.get(key);
            if (existing == null) grouped.put(key, item);
            else existing.setQuantity(existing.getQuantity() + item.getQuantity());
        }
        return List.copyOf(grouped.values());
    }

    private boolean matchesItem(InventoryItem item, String search, String category, String fragmentStage, String rarity) {
        String normalizedSearch = search == null ? "" : search.toLowerCase(Locale.ROOT).trim();
        String type = item.getItemType() == null ? "" : item.getItemType().name();
        String name = itemName(item);
        String code = itemCode(item);
        boolean textMatch = normalizedSearch.isEmpty() || List.of(name, type, code,
                item.getItemDefinition() == null ? "" : item.getItemDefinition().getCategory(),
                item.getItemDefinition() == null ? "" : item.getItemDefinition().getRarity())
                .stream().anyMatch(value -> value != null && value.toLowerCase(Locale.ROOT).contains(normalizedSearch));
        String resolvedCategory = resolvedCategory(item);
        boolean categoryMatch = "ALL".equalsIgnoreCase(category) || resolvedCategory.equalsIgnoreCase(category);
        String itemRarity = item.getItemDefinition() == null ? "" : String.valueOf(item.getItemDefinition().getRarity());
        boolean rarityMatch = "ALL".equalsIgnoreCase(rarity) || itemRarity.equalsIgnoreCase(rarity);
        boolean stageMatch = !"FRAGMENT".equalsIgnoreCase(category) || "ALL".equalsIgnoreCase(fragmentStage)
                || type.equalsIgnoreCase("FRAGMENT_" + fragmentStage);
        return textMatch && categoryMatch && rarityMatch && stageMatch;
    }

    private String resolvedCategory(InventoryItem item) {
        String type = item.getItemType() == null ? "" : item.getItemType().name();
        if (type.startsWith("FRAGMENT_")) return "FRAGMENT";
        if (item.getItemDefinition() != null && item.getItemDefinition().getCategory() != null) return item.getItemDefinition().getCategory().toUpperCase(Locale.ROOT);
        if (type.startsWith("DIGITAMA_")) return "DIGITAMA";
        if (type.startsWith("INCUBATOR_")) return "INCUBATOR";
        if ("LOOT_CHEST".equals(type)) return "CHEST";
        if ("EVOLUTION_MATERIAL".equals(type)) return "EVOLUTION_MATERIAL";
        if ("POTION_SMALL".equals(type) || type.startsWith("XP_DISC_") || type.startsWith("STORAGE_SLOT_") || "INCUBATION_SLOT_UNLOCK".equals(type)) return "CONSUMABLE";
        if ("TRAINING_STONE".equals(type) || "DATA_CORE".equals(type) || "REFINEMENT_STONE".equals(type)) return "MATERIAL";
        return "OTHER";
    }

    private Comparator<InventoryItem> itemComparator(String sort) {
        Comparator<InventoryItem> byName = Comparator.comparing(this::itemName, String.CASE_INSENSITIVE_ORDER).thenComparing(this::itemCode, String.CASE_INSENSITIVE_ORDER).thenComparing(InventoryItem::getId);
        return switch (sort == null ? "name-asc" : sort) {
            case "name-desc" -> byName.reversed();
            case "quantity-desc" -> Comparator.comparingInt(InventoryItem::getQuantity).reversed().thenComparing(byName);
            case "quantity-asc" -> Comparator.comparingInt(InventoryItem::getQuantity).thenComparing(byName);
            case "category-asc" -> Comparator.comparing(this::resolvedCategory, String.CASE_INSENSITIVE_ORDER).thenComparing(byName);
            case "rarity-desc" -> Comparator.comparingInt((InventoryItem item) -> rarityRank(item.getItemDefinition() == null ? null : item.getItemDefinition().getRarity())).reversed().thenComparing(byName);
            case "rarity-asc" -> Comparator.comparingInt((InventoryItem item) -> rarityRank(item.getItemDefinition() == null ? null : item.getItemDefinition().getRarity())).thenComparing(byName);
            default -> byName;
        };
    }

    private int rarityRank(String rarity) {
        return switch (rarity == null ? "" : rarity.toUpperCase(Locale.ROOT)) {
            case "COMMON" -> 1; case "RARE" -> 2; case "EPIC" -> 3; case "LEGENDARY" -> 4; default -> 0;
        };
    }

    private int categoryOrder(InventoryItem item) {
        String category = item.getItemDefinition() == null ? legacyCategory(item.getItemType()) : item.getItemDefinition().getCategory();
        return switch (category == null ? "" : category.toUpperCase()) {
            case "CONSUMABLE" -> 10;
            case "MATERIAL" -> 20;
            case "EVOLUTION_MATERIAL" -> 30;
            case "FRAGMENT" -> 40;
            case "DIGITAMA" -> 50;
            case "INCUBATOR" -> 60;
            case "CHEST" -> 70;
            default -> 99;
        };
    }

    private String itemName(InventoryItem item) {
        if (item.getItemDefinition() != null && item.getItemDefinition().getName() != null) {
            return item.getItemDefinition().getName();
        }
        return item.getItemType() == null ? "" : item.getItemType().name();
    }

    private String itemCode(InventoryItem item) {
        if (item.getItemDefinition() != null && item.getItemDefinition().getCode() != null) {
            return item.getItemDefinition().getCode();
        }
        return item.getItemType() == null ? "" : item.getItemType().name();
    }

    private String legacyCategory(ItemType itemType) {
        if (itemType == null) return "";
        if (itemType.name().startsWith("DIGITAMA_")) return "DIGITAMA";
        if (itemType.name().startsWith("INCUBATOR_")) return "INCUBATOR";
        if (itemType == ItemType.LOOT_CHEST) return "CHEST";
        if (itemType == ItemType.EVOLUTION_MATERIAL) return "EVOLUTION_MATERIAL";
        if (itemType == ItemType.POTION_SMALL || itemType == ItemType.INCUBATION_SLOT_UNLOCK
                || itemType.name().startsWith("XP_DISC_")) return "CONSUMABLE";
        if (itemType == ItemType.TRAINING_STONE || itemType == ItemType.DATA_CORE || itemType == ItemType.REFINEMENT_STONE) return "MATERIAL";
        return "OTHER";
    }

    @PostMapping("/chests/open")
    public ResponseEntity<ChestOpeningResponse> openChest(@RequestHeader("Authorization") String authorization, @RequestBody @Valid OpenChestRequest request) {
        return ResponseEntity.ok(openChestUseCase.execute(authorization, request));
    }

    @PostMapping("/rarity-reroll/start")
    public ResponseEntity<RarityRerollResponse> startRarityReroll(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(rarityRerollUseCase.start(authorization));
    }

    @PostMapping("/rarity-reroll/{id}/accept")
    public ResponseEntity<RarityRerollResponse> acceptRarityReroll(@RequestHeader("Authorization") String authorization, @PathVariable UUID id) {
        return ResponseEntity.ok(rarityRerollUseCase.accept(authorization, id));
    }

    @PostMapping("/rarity-reroll/{id}/keep")
    public ResponseEntity<RarityRerollResponse> keepRarityReroll(@RequestHeader("Authorization") String authorization, @PathVariable UUID id) {
        return ResponseEntity.ok(rarityRerollUseCase.keep(authorization, id));
    }

    @PostMapping("/use")
    public ResponseEntity<UseItemResponse> useItem(@RequestHeader("Authorization") String authorization, @RequestBody @Valid UseItemRequest request) {
        return ResponseEntity.ok(useItemUseCase.execute(authorization, request.itemType(), request.quantity()));
    }

    public InventoryController(final InventoryRepository repository, final UseItemUseCase useItemUseCase, final OpenChestUseCase openChestUseCase, final RarityRerollUseCase rarityRerollUseCase) {
        this.repository = repository;
        this.useItemUseCase = useItemUseCase;
        this.openChestUseCase = openChestUseCase;
        this.rarityRerollUseCase = rarityRerollUseCase;
    }
}
