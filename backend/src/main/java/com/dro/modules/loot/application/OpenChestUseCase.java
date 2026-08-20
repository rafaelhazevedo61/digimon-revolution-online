package com.dro.modules.loot.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.loot.api.dto.request.OpenChestRequest;
import com.dro.modules.loot.api.dto.response.ChestOpeningItemResponse;
import com.dro.modules.loot.api.dto.response.ChestOpeningResponse;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.ChestLootRoller;
import com.dro.modules.loot.domain.ChestOpeningEntity;
import com.dro.modules.loot.domain.ChestOpeningItemEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.loot.infra.ChestOpeningRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abre baús de forma atômica e idempotente.
 *
 * <p>O caso de uso bloqueia o Digimon ativo e o item de baú, sorteia o resultado,
 * debita o baú, credita as recompensas, registra a abertura e enfileira a
 * auditoria positiva na mesma transação PostgreSQL.</p>
 */
@Service
@RequiredArgsConstructor
public class OpenChestUseCase {

    private static final String OPENING_SOURCE = "PLAYER_INVENTORY";

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final ChestDefinitionRepository chestDefinitionRepository;
    private final ChestOpeningRepository chestOpeningRepository;
    private final ChestLootRoller chestLootRoller;
    private final TransactionAuditPublisher transactionAuditPublisher;

    /**
     * Abre um baú do inventário do Digimon ativo do jogador.
     *
     * @param token token JWT do jogador
     * @param request código do baú e chave idempotente da requisição
     * @return resultado persistido da abertura
     */
    @Transactional
    public ChestOpeningResponse execute(String token, OpenChestRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        validateRequest(request);

        ChestOpeningEntity previousOpening = chestOpeningRepository
                .findByRequestId(request.requestId())
                .orElse(null);
        if (previousOpening != null) {
            validateRetryOwnership(previousOpening, playerId, request.chestCode());
            return toResponse(previousOpening, true);
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon activeDigimon = findLockedActiveDigimon(player, playerId);
        ChestDefinitionEntity chest = chestDefinitionRepository
                .findWithCatalogByCode(request.chestCode())
                .filter(ChestDefinitionEntity::isActive)
                .orElseThrow(() -> new NotFoundException("Chest not found or inactive"));

        InventoryItem chestInventory = inventoryRepository
                .findByDigimonIdAndItemDefinitionIdForUpdate(
                        activeDigimon.getId(),
                        chest.getItemDefinition().getId()
                )
                .orElseThrow(() -> new NotFoundException("Chest not found in inventory"));

        if (chestInventory.getItemType() != ItemType.LOOT_CHEST || chestInventory.getQuantity() <= 0) {
            throw new UnprocessableException("No chest available in inventory");
        }

        ChestLootRoller.ChestLootRoll roll = chestLootRoller.roll(chest.getLootTable());
        List<ChestOpeningItemEntity> openingItems = new ArrayList<>();
        for (ChestLootRoller.ChestLootItem reward : roll.items()) {
            creditReward(activeDigimon, reward);
            openingItems.add(ChestOpeningItemEntity.builder()
                    .itemType(reward.itemType())
                    .materialCode(reward.materialCode())
                    .quantity(reward.quantity())
                    .build());
        }

        consumeChest(chestInventory);

        ChestOpeningEntity openingToPersist = ChestOpeningEntity.builder()
                .requestId(request.requestId())
                .playerId(playerId)
                .chestDefinition(chest)
                .rarity(roll.rarity())
                .source(OPENING_SOURCE)
                .items(openingItems)
                .build();
        openingItems.forEach(item -> item.setChestOpening(openingToPersist));
        ChestOpeningEntity opening = chestOpeningRepository.saveAndFlush(openingToPersist);

        transactionAuditPublisher.success(
                "chest-opening:" + opening.getId(),
                "CHEST_OPENED",
                "ChestOpening",
                String.valueOf(opening.getId()),
                buildAuditPayload(opening, activeDigimon, roll)
        );

        return toResponse(opening, false);
    }

    private void validateRequest(OpenChestRequest request) {
        if (request == null || request.chestCode() == null || request.chestCode().isBlank()
                || request.requestId() == null || request.requestId().isBlank()) {
            throw new BadRequestException("Chest code and request id are required");
        }
    }

    private void validateRetryOwnership(
            ChestOpeningEntity previousOpening,
            UUID playerId,
            String chestCode
    ) {
        if (!previousOpening.getPlayerId().equals(playerId)
                || !previousOpening.getChestDefinition().getCode().equals(chestCode)) {
            throw new ConflictException("Request id is already associated with another chest opening");
        }
    }

    private Digimon findLockedActiveDigimon(Player player, UUID playerId) {
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = digimonRepository.findByIdForUpdate(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));
        if (!playerId.equals(digimon.getPlayerId())) {
            throw new ConflictException("Active digimon ownership changed");
        }
        return digimon;
    }

    private void creditReward(Digimon digimon, ChestLootRoller.ChestLootItem reward) {
        String itemCode = reward.materialCode() == null
                ? reward.itemType().name()
                : reward.materialCode();
        ItemDefinition itemDefinition = itemDefinitionRepository.findByCode(itemCode)
                .orElseThrow(() -> new UnprocessableException("Reward item is not defined: " + itemCode));

        InventoryItem inventoryItem = inventoryRepository
                .findByDigimonIdAndItemDefinitionIdForUpdate(digimon.getId(), itemDefinition.getId())
                .orElse(null);
        int currentQuantity = inventoryItem == null ? 0 : inventoryItem.getQuantity();
        int newQuantity = currentQuantity + reward.quantity();
        if (itemDefinition.getMaxStack() != null && newQuantity > itemDefinition.getMaxStack()) {
            throw new UnprocessableException(
                    "Cannot exceed max stack of " + itemDefinition.getMaxStack()
                            + " for item " + itemDefinition.getCode());
        }

        if (inventoryItem == null) {
            inventoryRepository.save(InventoryItem.builder()
                    .id(UUID.randomUUID())
                    .digimonId(digimon.getId())
                    .itemType(reward.itemType())
                    .itemDefinition(itemDefinition)
                    .quantity(reward.quantity())
                    .build());
        } else {
            inventoryItem.setQuantity(newQuantity);
            inventoryRepository.save(inventoryItem);
        }
    }

    private void consumeChest(InventoryItem chestInventory) {
        int remaining = chestInventory.getQuantity() - 1;
        if (remaining == 0) {
            inventoryRepository.delete(chestInventory);
        } else {
            chestInventory.setQuantity(remaining);
            inventoryRepository.save(chestInventory);
        }
    }

    private Map<String, Object> buildAuditPayload(
            ChestOpeningEntity opening,
            Digimon digimon,
            ChestLootRoller.ChestLootRoll roll
    ) {
        List<Map<String, Object>> items = roll.items().stream()
                .map(reward -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("itemType", reward.itemType().name());
                    if (reward.materialCode() != null) {
                        item.put("materialCode", reward.materialCode());
                    }
                    item.put("quantity", reward.quantity());
                    return item;
                })
                .toList();

        return Map.of(
                "module", "loot",
                "operation", "openChest",
                "playerId", opening.getPlayerId().toString(),
                "digimonId", digimon.getId().toString(),
                "requestId", opening.getRequestId(),
                "chestCode", opening.getChestDefinition().getCode(),
                "rarity", opening.getRarity().name(),
                "items", items,
                "summary", "Chest opened successfully"
        );
    }

    private ChestOpeningResponse toResponse(ChestOpeningEntity opening, boolean replayed) {
        List<ChestOpeningItemResponse> items = opening.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        ChestDefinitionEntity chest = opening.getChestDefinition();
        String message = replayed
                ? "Esta abertura já havia sido processada. O resultado original foi retornado."
                : "Baú aberto com sucesso!";
        return new ChestOpeningResponse(
                opening.getRequestId(),
                chest.getCode(),
                chest.getName(),
                opening.getRarity(),
                items,
                replayed,
                message
        );
    }

    private ChestOpeningItemResponse toItemResponse(ChestOpeningItemEntity item) {
        String itemCode = item.getMaterialCode() == null
                ? item.getItemType().name()
                : item.getMaterialCode();
        String itemName = itemDefinitionRepository.findByCode(itemCode)
                .map(ItemDefinition::getName)
                .orElse(itemCode);
        return new ChestOpeningItemResponse(
                itemCode,
                itemName,
                item.getItemType(),
                item.getMaterialCode(),
                item.getQuantity()
        );
    }
}
