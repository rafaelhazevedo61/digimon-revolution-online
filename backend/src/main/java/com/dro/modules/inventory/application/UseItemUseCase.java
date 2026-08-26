package com.dro.modules.inventory.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.IncubatorRules;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Inventário.
 */
@Service
public class UseItemUseCase {
    private final InventoryRepository inventoryRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void execute(String token, ItemType type) {
        if (isIncubationOnly(type)) {
            throw new BadRequestException("Digitamas e incubadoras devem ser usados pela tela de incubação");
        }
        UUID playerId = TokenExtractor.extractPlayerId(token);
        if (type == ItemType.INCUBATION_SLOT_UNLOCK) {
            unlockIncubationSlot(playerId);
            return;
        }

        var player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        InventoryItem item = inventoryRepository.findByDigimonIdAndItemType(digimon.getId(), type).orElseThrow(() -> new NotFoundException("Item not found"));
        if (item.getQuantity() <= 0) {
            throw new UnprocessableException("No item available");
        }
        item.setQuantity(item.getQuantity() - 1);
        digimon.gainExperience(50);
        inventoryRepository.save(item);
        digimonRepository.save(digimon);
    }

    private void unlockIncubationSlot(UUID playerId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getUnlockedIncubationSlots() >= IncubatorRules.TOTAL_SLOTS) {
            throw new BadRequestException("Todos os slots de incubação já estão desbloqueados");
        }
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));
        InventoryItem item = inventoryRepository
                .findByDigimonIdAndItemTypeForUpdate(digimon.getId(), ItemType.INCUBATION_SLOT_UNLOCK)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        if (item.getQuantity() <= 0) {
            throw new UnprocessableException("No item available");
        }

        item.setQuantity(item.getQuantity() - 1);
        if (item.getQuantity() == 0) {
            inventoryRepository.delete(item);
        } else {
            inventoryRepository.save(item);
        }
        player.setUnlockedIncubationSlots(player.getUnlockedIncubationSlots() + 1);
        playerRepository.save(player);
    }

    private boolean isIncubationOnly(ItemType type) {
        return switch (type) {
            case DIGITAMA_STARTER, DIGITAMA_FIRE, DIGITAMA_WATER, DIGITAMA_NATURE, INCUBATOR_COMMON, INCUBATOR_RARE, INCUBATOR_EPIC -> true;
            default -> false;
        };
    }

    public UseItemUseCase(final InventoryRepository inventoryRepository, final DigimonRepository digimonRepository, final PlayerRepository playerRepository) {
        this.inventoryRepository = inventoryRepository;
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
    }
}
