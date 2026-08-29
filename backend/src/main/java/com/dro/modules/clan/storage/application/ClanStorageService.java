package com.dro.modules.clan.storage.application;

import com.dro.modules.clan.application.ClanAuthorizationService;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanUpgradeType;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.ClanUpgradeTypeRepository;
import com.dro.modules.clan.storage.api.dto.request.ClanStorageItemRequest;
import com.dro.modules.clan.storage.api.dto.response.ClanStorageHistoryResponse;
import com.dro.modules.clan.storage.api.dto.response.ClanStorageItemResponse;
import com.dro.modules.clan.storage.api.dto.response.ClanStorageResponse;
import com.dro.modules.clan.storage.domain.ClanStorageHistory;
import com.dro.modules.clan.storage.domain.ClanStorageItem;
import com.dro.modules.clan.storage.infra.ClanStorageHistoryRepository;
import com.dro.modules.clan.storage.infra.ClanStorageItemRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClanStorageService {
    private static final String CAPACITY_UPGRADE_CODE = "CLAN_STORAGE_CAPACITY";
    private static final int INITIAL_CAPACITY = 20;
    private static final int CAPACITY_PER_LEVEL = 10;
    private static final int HISTORY_LIMIT = 100;

    private final ClanAuthorizationService authorization;
    private final ClanRepository clanRepository;
    private final ClanUpgradeTypeRepository upgradeTypeRepository;
    private final ClanBonusService clanBonusService;
    private final PlayerRepository playerRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final ClanStorageItemRepository storageItemRepository;
    private final ClanStorageHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public ClanStorageResponse get(String token, UUID clanId) {
        Player actor = findPlayer(TokenExtractor.extractPlayerId(token));
        Clan clan = authorization.getClan(clanId);
        authorization.assertInClan(actor, clan);
        return buildResponse(clan);
    }

    @Transactional
    public ClanStorageResponse deposit(String token, UUID clanId, ClanStorageItemRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player actor = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Clan clan = clanRepository.findByIdForUpdate(clanId)
                .orElseThrow(() -> new NotFoundException("Clan not found"));
        authorization.assertCanDepositStorage(actor, clan);

        int quantity = requireQuantity(request);
        ItemDefinition definition = findTradableDefinition(request.itemDefinitionId());
        InventoryItem personalItem = inventoryRepository
                .findByPlayerIdAndItemDefinitionIdForUpdate(playerId, definition.getId())
                .orElseThrow(() -> new BadRequestException("Item not found in personal inventory"));
        if (personalItem.getQuantity() < quantity) {
            throw new BadRequestException("Not enough items in personal inventory");
        }

        List<ClanStorageItem> stacks = storageItemRepository
                .findByClanIdAndItemDefinitionIdForUpdate(clanId, definition.getId());
        int maxStack = resolveMaxStack(definition);
        int requiredNewSlots = calculateRequiredNewSlots(stacks, quantity, maxStack);
        int usedSlots = Math.toIntExact(storageItemRepository.countByClanId(clanId));
        if (usedSlots + requiredNewSlots > getCapacity(clanId)) {
            throw new BadRequestException("Clan storage has no available slots");
        }

        personalItem.setQuantity(personalItem.getQuantity() - quantity);
        if (personalItem.getQuantity() == 0) {
            inventoryRepository.delete(personalItem);
        } else {
            inventoryRepository.save(personalItem);
        }
        addToStacks(clanId, definition, stacks, quantity, maxStack);
        historyRepository.save(ClanStorageHistory.create(
                clanId, actor.getId(), actor.getUsername(), "DEPOSIT", definition, quantity
        ));
        return buildResponse(clan);
    }

    @Transactional
    public ClanStorageResponse withdraw(String token, UUID clanId, ClanStorageItemRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player actor = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Clan clan = clanRepository.findByIdForUpdate(clanId)
                .orElseThrow(() -> new NotFoundException("Clan not found"));
        authorization.assertCanWithdrawStorage(actor, clan);

        int quantity = requireQuantity(request);
        ItemDefinition definition = findTradableDefinition(request.itemDefinitionId());
        List<ClanStorageItem> stacks = storageItemRepository
                .findByClanIdAndItemDefinitionIdForUpdate(clanId, definition.getId());
        int storedQuantity = stacks.stream().mapToInt(ClanStorageItem::getQuantity).sum();
        if (storedQuantity < quantity) {
            throw new BadRequestException("Not enough items in clan storage");
        }

        int maxStack = resolveMaxStack(definition);
        InventoryItem personalItem = inventoryRepository
                .findByPlayerIdAndItemDefinitionIdForUpdate(playerId, definition.getId())
                .orElse(null);
        int currentPersonalQuantity = personalItem == null ? 0 : personalItem.getQuantity();
        if (currentPersonalQuantity > maxStack - quantity) {
            throw new BadRequestException("Personal inventory has no room for this quantity");
        }

        removeFromStacks(stacks, quantity);
        if (personalItem == null) {
            personalItem = InventoryItem.builder()
                    .id(UUID.randomUUID())
                    .playerId(playerId)
                    .itemType(resolveItemType(definition))
                    .itemDefinition(definition)
                    .quantity(quantity)
                    .build();
        } else {
            personalItem.setQuantity(currentPersonalQuantity + quantity);
        }
        inventoryRepository.save(personalItem);
        historyRepository.save(ClanStorageHistory.create(
                clanId, actor.getId(), actor.getUsername(), "WITHDRAW", definition, quantity
        ));
        return buildResponse(clan);
    }

    private int requireQuantity(ClanStorageItemRequest request) {
        if (request == null || request.quantity() == null || request.quantity() < 1 || request.quantity() > 999) {
            throw new BadRequestException("Quantity must be between 1 and 999");
        }
        return request.quantity();
    }

    private ItemDefinition findTradableDefinition(Long itemDefinitionId) {
        ItemDefinition definition = itemDefinitionRepository.findById(itemDefinitionId)
                .orElseThrow(() -> new NotFoundException("Item definition not found"));
        if (!definition.isTradable()) {
            throw new BadRequestException("Only tradable items can be stored in the clan storage");
        }
        return definition;
    }

    private int resolveMaxStack(ItemDefinition definition) {
        if (definition.getMaxStack() == null || definition.getMaxStack() < 1) {
            return 1;
        }
        return definition.getMaxStack();
    }

    private int calculateRequiredNewSlots(List<ClanStorageItem> stacks, int quantity, int maxStack) {
        int remaining = quantity;
        for (ClanStorageItem stack : stacks) {
            remaining -= Math.min(remaining, Math.max(0, maxStack - stack.getQuantity()));
            if (remaining == 0) return 0;
        }
        return (remaining + maxStack - 1) / maxStack;
    }

    private void addToStacks(UUID clanId, ItemDefinition definition, List<ClanStorageItem> stacks,
                             int quantity, int maxStack) {
        int remaining = quantity;
        for (ClanStorageItem stack : stacks) {
            if (remaining == 0) break;
            int space = Math.max(0, maxStack - stack.getQuantity());
            int amount = Math.min(space, remaining);
            if (amount > 0) {
                stack.setQuantity(stack.getQuantity() + amount);
                storageItemRepository.save(stack);
                remaining -= amount;
            }
        }
        while (remaining > 0) {
            int amount = Math.min(maxStack, remaining);
            storageItemRepository.save(ClanStorageItem.create(clanId, definition, amount));
            remaining -= amount;
        }
    }

    private void removeFromStacks(List<ClanStorageItem> stacks, int quantity) {
        int remaining = quantity;
        for (ClanStorageItem stack : stacks) {
            if (remaining == 0) break;
            int amount = Math.min(stack.getQuantity(), remaining);
            stack.setQuantity(stack.getQuantity() - amount);
            if (stack.getQuantity() == 0) {
                storageItemRepository.delete(stack);
            } else {
                storageItemRepository.save(stack);
            }
            remaining -= amount;
        }
    }

    private ClanStorageResponse buildResponse(Clan clan) {
        List<ClanStorageItemResponse> items = storageItemRepository
                .findByClanIdOrderByCreatedAtAsc(clan.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();
        List<ClanStorageHistoryResponse> history = historyRepository
                .findRecentByClanId(clan.getId(), HISTORY_LIMIT)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
        int capacityUpgradeLevel = clanBonusService.getUpgradeLevel(clan.getId(), CAPACITY_UPGRADE_CODE);
        ClanUpgradeType upgradeType = upgradeTypeRepository.findById(CAPACITY_UPGRADE_CODE).orElse(null);
        int maxUpgradeLevel = upgradeType == null ? 10 : upgradeType.getMaxLevel();
        int nextCost = upgradeType == null ? 0 : clanBonusService.calculateNextCost(upgradeType, capacityUpgradeLevel);
        int capacity = INITIAL_CAPACITY + (capacityUpgradeLevel * CAPACITY_PER_LEVEL);
        int usedSlots = items.size();
        return new ClanStorageResponse(
                clan.getId(), capacity, usedSlots, Math.max(0, capacity - usedSlots),
                clan.getHonorMarks(), capacityUpgradeLevel, maxUpgradeLevel, nextCost,
                items, history
        );
    }

    private int getCapacity(UUID clanId) {
        return INITIAL_CAPACITY + clanBonusService.getUpgradeLevel(clanId, CAPACITY_UPGRADE_CODE) * CAPACITY_PER_LEVEL;
    }

    private ClanStorageItemResponse toItemResponse(ClanStorageItem item) {
        ItemDefinition definition = item.getItemDefinition();
        return new ClanStorageItemResponse(
                item.getId(), definition.getId(), definition.getCode(), definition.getName(),
                definition.getDescription(), definition.getIcon(), definition.getCategory(),
                definition.isStackable(), definition.getMaxStack(), definition.getRarity(), item.getQuantity()
        );
    }

    private ClanStorageHistoryResponse toHistoryResponse(ClanStorageHistory entry) {
        return new ClanStorageHistoryResponse(
                entry.getId(), entry.getAction(), entry.getActorPlayerId(), entry.getActorUsername(),
                entry.getItemDefinition().getId(), entry.getItemCode(), entry.getItemName(),
                entry.getQuantity(), entry.getCreatedAt()
        );
    }

    private ItemType resolveItemType(ItemDefinition definition) {
        if ("CHEST".equalsIgnoreCase(definition.getCategory())) return ItemType.LOOT_CHEST;
        try {
            return ItemType.valueOf(definition.getCode());
        } catch (IllegalArgumentException exception) {
            return ItemType.EVOLUTION_MATERIAL;
        }
    }

    private Player findPlayer(UUID playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
    }

    public ClanStorageService(final ClanAuthorizationService authorization,
                              final ClanRepository clanRepository,
                              final ClanUpgradeTypeRepository upgradeTypeRepository,
                              final ClanBonusService clanBonusService,
                              final PlayerRepository playerRepository,
                              final InventoryRepository inventoryRepository,
                              final ItemDefinitionRepository itemDefinitionRepository,
                              final ClanStorageItemRepository storageItemRepository,
                              final ClanStorageHistoryRepository historyRepository) {
        this.authorization = authorization;
        this.clanRepository = clanRepository;
        this.upgradeTypeRepository = upgradeTypeRepository;
        this.clanBonusService = clanBonusService;
        this.playerRepository = playerRepository;
        this.inventoryRepository = inventoryRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.storageItemRepository = storageItemRepository;
        this.historyRepository = historyRepository;
    }
}
