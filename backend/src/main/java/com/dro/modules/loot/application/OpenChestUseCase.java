package com.dro.modules.loot.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.application.GrantEquipmentUseCase;
import com.dro.modules.equipment.domain.EquipmentRarityRules;
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
import com.dro.modules.loot.domain.LootRarity;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Abre baús de forma atômica e idempotente.
 *
 * <p>O caso de uso bloqueia o jogador, o Digimon ativo e os itens de inventário, sorteia o resultado,
 * debita os baús, credita as recompensas, registra a abertura e enfileira a
 * auditoria positiva na mesma transação PostgreSQL.</p>
 */
@Service
public class OpenChestUseCase {
    private static final String OPENING_SOURCE = "PLAYER_INVENTORY";
    private static final int MAX_BATCH_QUANTITY = 999;
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final ChestDefinitionRepository chestDefinitionRepository;
    private final ChestOpeningRepository chestOpeningRepository;
    private final ChestLootRoller chestLootRoller;
    private final TransactionAuditPublisher transactionAuditPublisher;
    private final GrantEquipmentUseCase grantEquipmentUseCase;

    /**
     * Abre um ou mais baús do inventário do Digimon ativo do jogador.
     *
     * @param token token JWT do jogador
     * @param request código do baú, chave idempotente e quantidade solicitada
     * @return resultado persistido da abertura
     */
    @Transactional
    public ChestOpeningResponse execute(String token, OpenChestRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        validateRequest(request);
        int quantity = request.requestedQuantity();
        boolean ignoreMaxStackItems = request.shouldIgnoreMaxStackItems();
        // Serialize all inventory mutations for this player before checking idempotency.
        // A retry therefore observes the opening after the first transaction commits.
        Player player = playerRepository.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        ChestOpeningEntity previousOpening = chestOpeningRepository.findByRequestId(request.requestId()).orElse(null);
        if (previousOpening != null) {
            validateRetryOwnership(previousOpening, playerId, request.chestCode());
            return toResponse(previousOpening, true, List.of());
        }
        Digimon activeDigimon = findLockedActiveDigimon(player, playerId);
        ChestDefinitionEntity chest = chestDefinitionRepository.findWithCatalogByCode(request.chestCode()).filter(ChestDefinitionEntity::isActive).orElseThrow(() -> new NotFoundException("Chest not found or inactive"));
        InventoryItem chestInventory = inventoryRepository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, chest.getItemDefinition().getId()).orElseThrow(() -> new NotFoundException("Chest not found in inventory"));
        if (chestInventory.getItemType() != ItemType.LOOT_CHEST || chestInventory.getQuantity() < quantity) {
            throw new UnprocessableException("Você não possui baús suficientes para abrir essa quantidade.");
        }

        List<ChestOpeningItemEntity> openingItems = new ArrayList<>();
        // Nomes dos itens cujo excedente foi descartado por já estarem no limite máximo de estoque
        // (preserva ordem de ocorrência e evita duplicatas quando o mesmo item estoura em rolagens diferentes).
        Set<String> itemsAtStackLimit = new LinkedHashSet<>();
        LootRarity primaryRarity = null;
        for (int index = 0; index < quantity; index++) {
            ChestLootRoller.ChestLootRoll roll = chestLootRoller.roll(chest.getLootTable());
            if (primaryRarity == null) {
                primaryRarity = roll.rarity();
            }
            for (ChestLootRoller.ChestLootItem reward : roll.items()) {
                ChestLootRoller.ChestLootItem resolvedReward = resolveEquipmentRarity(reward);
                CreditResult creditResult = creditReward(playerId, activeDigimon, resolvedReward, ignoreMaxStackItems);
                if (creditResult.stackLimitReached()) {
                    itemsAtStackLimit.add(creditResult.itemName());
                }
                if (creditResult.quantity() > 0) {
                    mergeOpeningItem(openingItems, resolvedReward, creditResult.quantity());
                }
            }
        }
        consumeChest(chestInventory, quantity);

        ChestOpeningEntity openingToPersist = ChestOpeningEntity.builder()
                .requestId(request.requestId())
                .playerId(playerId)
                .chestDefinition(chest)
                .quantity(quantity)
                .rarity(primaryRarity)
                .source(OPENING_SOURCE)
                .items(openingItems)
                .build();
        openingItems.forEach(item -> item.setChestOpening(openingToPersist));
        ChestOpeningEntity opening = chestOpeningRepository.saveAndFlush(openingToPersist);
        transactionAuditPublisher.success("chest-opening:" + opening.getId(), "CHEST_OPENED", "ChestOpening", String.valueOf(opening.getId()), buildAuditPayload(opening, activeDigimon, itemsAtStackLimit));
        return toResponse(opening, false, List.copyOf(itemsAtStackLimit));
    }

    /**
     * Resultado do crédito de uma recompensa individual ao inventário.
     *
     * @param quantity quantidade efetivamente creditada (pode ser menor que a sorteada
     *                 quando {@code ignoreMaxStackItems} está ativo e o estoque estava perto do limite)
     * @param stackLimitReached {@code true} quando parte ou todo o excedente foi descartado por limite de estoque
     * @param itemName nome do item afetado, usado apenas quando {@code stackLimitReached} é {@code true}
     */
    private record CreditResult(int quantity, boolean stackLimitReached, String itemName) {
        private static CreditResult full(int quantity) {
            return new CreditResult(quantity, false, null);
        }
    }

    private void validateRequest(OpenChestRequest request) {
        if (request == null || request.chestCode() == null || request.chestCode().isBlank() || request.requestId() == null || request.requestId().isBlank()) {
            throw new BadRequestException("Chest code and request id are required");
        }
        if (request.requestedQuantity() < 1 || request.requestedQuantity() > MAX_BATCH_QUANTITY) {
            throw new BadRequestException("Chest quantity must be between 1 and " + MAX_BATCH_QUANTITY);
        }
    }

    private void validateRetryOwnership(ChestOpeningEntity previousOpening, UUID playerId, String chestCode) {
        if (!previousOpening.getPlayerId().equals(playerId) || !previousOpening.getChestDefinition().getCode().equals(chestCode)) {
            throw new ConflictException("Request id is already associated with another chest opening");
        }
    }

    private Digimon findLockedActiveDigimon(Player player, UUID playerId) {
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findByIdForUpdate(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        if (!playerId.equals(digimon.getPlayerId())) {
            throw new ConflictException("Active digimon ownership changed");
        }
        return digimon;
    }

    private ChestLootRoller.ChestLootItem resolveEquipmentRarity(ChestLootRoller.ChestLootItem reward) {
        if (reward.itemType() != ItemType.EQUIPMENT || reward.equipmentRarity() != null) {
            return reward;
        }
        return new ChestLootRoller.ChestLootItem(reward.rarity(), reward.itemType(), reward.materialCode(), reward.equipmentTemplateName(), EquipmentRarityRules.rollRarity(), reward.quantity());
    }

    private CreditResult creditReward(UUID playerId, Digimon activeDigimon, ChestLootRoller.ChestLootItem reward, boolean ignoreMaxStackItems) {
        if (reward.itemType() == ItemType.EQUIPMENT) {
            if (grantEquipmentUseCase == null) {
                throw new UnprocessableException("O suporte a recompensas de equipamento não está configurado.");
            }
            grantEquipmentUseCase.execute(activeDigimon.getId(), reward.equipmentTemplateName(), reward.equipmentRarity());
            return CreditResult.full(reward.quantity());
        }
        String itemCode = reward.materialCode() == null ? reward.itemType().name() : reward.materialCode();
        ItemDefinition itemDefinition = itemDefinitionRepository.findByCode(itemCode).orElseThrow(() -> new UnprocessableException("Reward item is not defined: " + itemCode));
        InventoryItem inventoryItem = inventoryRepository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, itemDefinition.getId()).orElse(null);
        int currentQuantity = inventoryItem == null ? 0 : inventoryItem.getQuantity();
        int newQuantity = currentQuantity + reward.quantity();
        int creditedQuantity = reward.quantity();
        boolean stackLimitReached = false;
        if (itemDefinition.getMaxStack() != null && newQuantity > itemDefinition.getMaxStack()) {
            if (!ignoreMaxStackItems) {
                throw new UnprocessableException("Não é possível exceder o limite máximo de " + itemDefinition.getMaxStack() + " unidades para o item " + itemDefinition.getName() + ".");
            }
            // Jogador optou por ignorar o limite: credita só o que couber e descarta o excedente.
            stackLimitReached = true;
            creditedQuantity = Math.max(0, itemDefinition.getMaxStack() - currentQuantity);
            newQuantity = currentQuantity + creditedQuantity;
        }
        if (creditedQuantity > 0) {
            if (inventoryItem == null) {
                inventoryRepository.save(InventoryItem.builder().id(UUID.randomUUID()).playerId(playerId).itemType(reward.itemType()).itemDefinition(itemDefinition).quantity(creditedQuantity).build());
            } else {
                inventoryItem.setQuantity(newQuantity);
                inventoryRepository.save(inventoryItem);
            }
        }
        return new CreditResult(creditedQuantity, stackLimitReached, itemDefinition.getName());
    }

    private void mergeOpeningItem(List<ChestOpeningItemEntity> openingItems, ChestLootRoller.ChestLootItem reward, int creditedQuantity) {
        ChestOpeningItemEntity existing = openingItems.stream()
                .filter(item -> item.getRarity() == reward.rarity()
                        && item.getItemType() == reward.itemType()
                        && Objects.equals(item.getMaterialCode(), reward.materialCode())
                        && Objects.equals(item.getEquipmentTemplateName(), reward.equipmentTemplateName())
                        && item.getEquipmentRarity() == reward.equipmentRarity())
                .findFirst()
                .orElse(null);
        if (existing == null) {
            openingItems.add(ChestOpeningItemEntity.builder()
                    .rarity(reward.rarity())
                    .itemType(reward.itemType())
                    .materialCode(reward.materialCode())
                    .equipmentTemplateName(reward.equipmentTemplateName())
                    .equipmentRarity(reward.equipmentRarity())
                    .quantity(creditedQuantity)
                    .build());
        } else {
            existing.setQuantity(existing.getQuantity() + creditedQuantity);
        }
    }

    private void consumeChest(InventoryItem chestInventory, int quantity) {
        int remaining = chestInventory.getQuantity() - quantity;
        if (remaining == 0) {
            inventoryRepository.delete(chestInventory);
        } else {
            chestInventory.setQuantity(remaining);
            inventoryRepository.save(chestInventory);
        }
    }

    private Map<String, Object> buildAuditPayload(ChestOpeningEntity opening, Digimon digimon, Set<String> itemsAtStackLimit) {
        List<Map<String, Object>> items = opening.getItems().stream().map(item -> {
            Map<String, Object> reward = new LinkedHashMap<>();
            reward.put("rarity", item.getRarity().name());
            reward.put("itemType", item.getItemType().name());
            if (item.getMaterialCode() != null) {
                reward.put("materialCode", item.getMaterialCode());
            }
            reward.put("quantity", item.getQuantity());
            if (item.getEquipmentTemplateName() != null) {
                reward.put("equipmentTemplateName", item.getEquipmentTemplateName());
                reward.put("equipmentRarity", item.getEquipmentRarity().name());
            }
            return reward;
        }).toList();
        Map<String, Object> payload = new LinkedHashMap<>(Map.of("module", "loot", "operation", "openChest", "playerId", opening.getPlayerId().toString(), "digimonId", digimon.getId().toString(), "requestId", opening.getRequestId(), "chestCode", opening.getChestDefinition().getCode(), "chestQuantity", opening.getQuantity(), "rarity", opening.getRarity().name()));
        payload.put("items", items);
        payload.put("summary", "Chest opened successfully");
        if (!itemsAtStackLimit.isEmpty()) {
            payload.put("itemsAtStackLimit", List.copyOf(itemsAtStackLimit));
        }
        return payload;
    }

    private ChestOpeningResponse toResponse(ChestOpeningEntity opening, boolean replayed, List<String> itemsAtStackLimit) {
        List<ChestOpeningItemResponse> items = opening.getItems().stream().map(this::toItemResponse).toList();
        ChestDefinitionEntity chest = opening.getChestDefinition();
        String message;
        if (replayed) {
            message = "Esta abertura já havia sido processada. O resultado original foi retornado.";
        } else if (!itemsAtStackLimit.isEmpty()) {
            message = "Baú aberto com sucesso! Alguns itens já estavam no limite máximo de estoque e o excedente foi descartado: " + String.join(", ", itemsAtStackLimit) + ".";
        } else {
            message = "Baú aberto com sucesso!";
        }
        return new ChestOpeningResponse(opening.getRequestId(), chest.getCode(), chest.getName(), opening.getRarity(), items, opening.getQuantity(), replayed, message, itemsAtStackLimit);
    }

    private ChestOpeningItemResponse toItemResponse(ChestOpeningItemEntity item) {
        String itemCode = item.getItemType() == ItemType.EQUIPMENT
                ? item.getEquipmentTemplateName()
                : item.getMaterialCode() == null ? item.getItemType().name() : item.getMaterialCode();
        String itemName = itemDefinitionRepository.findByCode(itemCode).map(ItemDefinition::getName).orElse(itemCode);
        return new ChestOpeningItemResponse(itemCode, itemName, item.getRarity(), item.getItemType(), item.getMaterialCode(), item.getQuantity(), item.getEquipmentTemplateName(), item.getEquipmentRarity());
    }

    public OpenChestUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final InventoryRepository inventoryRepository, final ItemDefinitionRepository itemDefinitionRepository, final ChestDefinitionRepository chestDefinitionRepository, final ChestOpeningRepository chestOpeningRepository, final ChestLootRoller chestLootRoller, final TransactionAuditPublisher transactionAuditPublisher, final GrantEquipmentUseCase grantEquipmentUseCase) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.inventoryRepository = inventoryRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.chestDefinitionRepository = chestDefinitionRepository;
        this.chestOpeningRepository = chestOpeningRepository;
        this.chestLootRoller = chestLootRoller;
        this.transactionAuditPublisher = transactionAuditPublisher;
        this.grantEquipmentUseCase = grantEquipmentUseCase;
    }
}
