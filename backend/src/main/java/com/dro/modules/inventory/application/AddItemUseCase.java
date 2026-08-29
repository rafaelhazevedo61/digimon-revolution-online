package com.dro.modules.inventory.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/** Concede itens ao inventário global do jogador. */
@Service
public class AddItemUseCase {
    private final InventoryRepository repository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final DigimonRepository digimonRepository;

    @Transactional
    public void execute(UUID ownerId, ItemType type, int quantity) {
        UUID playerId = resolvePlayerId(ownerId);
        ItemDefinition itemDefinition = itemDefinitionRepository.findByCode(type.name()).orElse(null);
        if (itemDefinition != null) {
            addMaterial(playerId, itemDefinition, quantity);
            return;
        }
        var existing = repository.findByPlayerIdAndItemType(playerId, type);
        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            repository.save(item);
        } else {
            repository.save(InventoryItem.builder().id(UUID.randomUUID()).playerId(playerId).itemType(type).quantity(quantity).build());
        }
    }

    @Transactional
    public void addMaterial(UUID ownerId, ItemDefinition itemDefinition, int quantity) {
        UUID playerId = resolvePlayerId(ownerId);
        var existing = repository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, itemDefinition.getId());
        int currentQuantity = existing.map(InventoryItem::getQuantity).orElse(0);
        int requestedQuantity = currentQuantity + quantity;
        int newQuantity = itemDefinition.getMaxStack() == null ? requestedQuantity : Math.min(requestedQuantity, itemDefinition.getMaxStack());
        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            item.setQuantity(newQuantity);
            repository.save(item);
        } else {
            repository.save(InventoryItem.builder().id(UUID.randomUUID()).playerId(playerId).itemType(resolveItemType(itemDefinition)).itemDefinition(itemDefinition).quantity(newQuantity).build());
        }
    }

    private UUID resolvePlayerId(UUID ownerId) {
        if (digimonRepository == null) return ownerId;
        return digimonRepository.findById(ownerId).map(digimon -> digimon.getPlayerId()).orElse(ownerId);
    }

    private ItemType resolveItemType(ItemDefinition itemDefinition) {
        if ("CHEST".equalsIgnoreCase(itemDefinition.getCategory())) return ItemType.LOOT_CHEST;
        try { return ItemType.valueOf(itemDefinition.getCode()); }
        catch (IllegalArgumentException e) { return ItemType.EVOLUTION_MATERIAL; }
    }

    @Autowired
    public AddItemUseCase(final InventoryRepository repository, final ItemDefinitionRepository itemDefinitionRepository, final DigimonRepository digimonRepository) {
        this.repository = repository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.digimonRepository = digimonRepository;
    }

    public AddItemUseCase(final InventoryRepository repository, final ItemDefinitionRepository itemDefinitionRepository) {
        this(repository, itemDefinitionRepository, null);
    }
}
