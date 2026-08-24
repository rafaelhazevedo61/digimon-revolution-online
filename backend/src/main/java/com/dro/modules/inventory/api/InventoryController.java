package com.dro.modules.inventory.api;

import com.dro.modules.inventory.api.dto.request.UseItemRequest;
import com.dro.modules.inventory.application.UseItemUseCase;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.api.dto.request.OpenChestRequest;
import com.dro.modules.loot.api.dto.response.ChestOpeningResponse;
import com.dro.modules.loot.application.OpenChestUseCase;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;
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
    private final PlayerRepository playerRepository;

    @GetMapping
    public ResponseEntity<?> getInventory(@RequestHeader("Authorization") String authorization) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);
        var player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        List<InventoryItem> items = repository.findByDigimonId(player.getActiveDigimonId());
        items.sort(Comparator.comparingInt(this::categoryOrder).thenComparing(this::itemName, String.CASE_INSENSITIVE_ORDER).thenComparing(this::itemCode, String.CASE_INSENSITIVE_ORDER).thenComparing(InventoryItem::getId));
        return ResponseEntity.ok(items);
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
        if (itemType == ItemType.POTION_SMALL) return "CONSUMABLE";
        if (itemType == ItemType.TRAINING_STONE || itemType == ItemType.DATA_CORE || itemType == ItemType.REFINEMENT_STONE) return "MATERIAL";
        return "OTHER";
    }

    @PostMapping("/chests/open")
    public ResponseEntity<ChestOpeningResponse> openChest(@RequestHeader("Authorization") String authorization, @RequestBody @Valid OpenChestRequest request) {
        return ResponseEntity.ok(openChestUseCase.execute(authorization, request));
    }

    @PostMapping("/use")
    public ResponseEntity<Void> useItem(@RequestHeader("Authorization") String authorization, @RequestBody @Valid UseItemRequest request) {
        useItemUseCase.execute(authorization, request.itemType());
        return ResponseEntity.ok().build();
    }

    public InventoryController(final InventoryRepository repository, final UseItemUseCase useItemUseCase, final OpenChestUseCase openChestUseCase, final PlayerRepository playerRepository) {
        this.repository = repository;
        this.useItemUseCase = useItemUseCase;
        this.openChestUseCase = openChestUseCase;
        this.playerRepository = playerRepository;
    }
}
