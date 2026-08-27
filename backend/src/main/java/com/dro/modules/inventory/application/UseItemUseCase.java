package com.dro.modules.inventory.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.IncubatorRules;
import com.dro.modules.inventory.api.dto.response.UseItemResponse;
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
    private static final int GENERIC_ITEM_XP = 50;
    private final InventoryRepository inventoryRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public UseItemResponse execute(String token, ItemType type) {
        if (isIncubationOnly(type)) {
            throw new BadRequestException("Digitamas e incubadoras devem ser usados pela tela de incubação");
        }
        UUID playerId = TokenExtractor.extractPlayerId(token);
        if (type == ItemType.INCUBATION_SLOT_UNLOCK) {
            return unlockIncubationSlot(playerId);
        }

        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findByIdForUpdate(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        InventoryItem item = inventoryRepository.findByDigimonIdAndItemTypeForUpdate(digimon.getId(), type).orElseThrow(() -> new NotFoundException("Item not found"));
        if (item.getQuantity() <= 0) {
            throw new UnprocessableException("No item available");
        }

        int previousLevel = digimon.getLevel();
        int xpGranted;
        if (isXpDisk(type)) {
            int xpToNextLevel = digimon.getExperienceToNextLevel();
            if (xpToNextLevel <= 0) {
                throw new BadRequestException("Este Digimon já está no nível máximo");
            }
            xpGranted = calculateXpDiskAmount(xpToNextLevel, xpDiskPercentage(type));
            digimon.grantDirectExperience(xpGranted);
        } else {
            xpGranted = GENERIC_ITEM_XP;
            digimon.gainExperience(xpGranted);
        }

        consumeOne(item);
        digimonRepository.save(digimon);
        return new UseItemResponse(
                type,
                xpGranted,
                previousLevel,
                digimon.getLevel(),
                digimon.getLevel() > previousLevel,
                isXpDisk(type) ? "Disco de XP utilizado com sucesso." : "Item utilizado com sucesso."
        );
    }

    private int calculateXpDiskAmount(int xpToNextLevel, int percentage) {
        return Math.max(1, (int) Math.floor((double) xpToNextLevel * percentage / 100.0));
    }

    private void consumeOne(InventoryItem item) {
        item.setQuantity(item.getQuantity() - 1);
        if (item.getQuantity() == 0) {
            inventoryRepository.delete(item);
        } else {
            inventoryRepository.save(item);
        }
    }

    private UseItemResponse unlockIncubationSlot(UUID playerId) {
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

        consumeOne(item);
        player.setUnlockedIncubationSlots(player.getUnlockedIncubationSlots() + 1);
        playerRepository.save(player);
        return new UseItemResponse(
                ItemType.INCUBATION_SLOT_UNLOCK,
                0,
                digimon.getLevel(),
                digimon.getLevel(),
                false,
                "Slot de incubação desbloqueado!"
        );
    }

    private boolean isXpDisk(ItemType type) {
        return switch (type) {
            case XP_DISC_1, XP_DISC_3, XP_DISC_5, XP_DISC_10, XP_DISC_15, XP_DISC_20 -> true;
            default -> false;
        };
    }

    private int xpDiskPercentage(ItemType type) {
        return switch (type) {
            case XP_DISC_1 -> 1;
            case XP_DISC_3 -> 3;
            case XP_DISC_5 -> 5;
            case XP_DISC_10 -> 10;
            case XP_DISC_15 -> 15;
            case XP_DISC_20 -> 20;
            default -> throw new IllegalArgumentException("Item is not an XP disk: " + type);
        };
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
