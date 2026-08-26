package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.DigitamaRules;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.domain.IncubatorRules;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Incubação.
 */
@Service
public class StartIncubationUseCase {
    private final IncubationRepository incubationRepository;
    private final InventoryRepository inventoryRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void execute(String token, int slotNumber, ItemType digitamaType, ItemType incubatorType) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        if (!IncubatorRules.isValidSlot(slotNumber)) {
            throw new BadRequestException("Invalid incubation slot");
        }

        // Serializa starts concorrentes do mesmo jogador antes de consultar o slot
        // e consumir os itens.
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        int unlockedSlots = player.getUnlockedIncubationSlots();
        if (!IncubatorRules.isUnlocked(slotNumber, unlockedSlots)) {
            throw new ConflictException("Incubation slot is locked");
        }
        if (incubationRepository.findByPlayerIdAndSlotNumberAndStatusNot(
                playerId, slotNumber, IncubationStatus.CLAIMED
        ).isPresent()) {
            throw new ConflictException("Incubation slot is already occupied");
        }

        if (!DigitamaRules.isDigitama(digitamaType)) {
            throw new BadRequestException("Invalid digitama");
        }
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        UUID digimonId = player.getActiveDigimonId();

        InventoryItem digitamaItem = inventoryRepository
                .findByDigimonIdAndItemType(digimonId, digitamaType)
                .orElseThrow(() -> new NotFoundException("Digitama not found"));
        if (digitamaItem.getQuantity() <= 0) {
            throw new UnprocessableException("No digitama available");
        }

        InventoryItem incubatorItem = inventoryRepository
                .findByDigimonIdAndItemType(digimonId, incubatorType)
                .orElseThrow(() -> new NotFoundException("Incubator not found"));
        if (incubatorItem.getQuantity() <= 0) {
            throw new UnprocessableException("No incubator available");
        }

        digitamaItem.setQuantity(digitamaItem.getQuantity() - 1);
        incubatorItem.setQuantity(incubatorItem.getQuantity() - 1);
        inventoryRepository.save(digitamaItem);
        inventoryRepository.save(incubatorItem);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finishAt = now.plus(IncubatorRules.getIncubationTime(incubatorType));
        Incubation incubation = Incubation.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .slotNumber(slotNumber)
                .digitamaType(digitamaType)
                .incubatorType(incubatorType)
                .startedAt(now)
                .finishAt(finishAt)
                .status(IncubationStatus.IN_PROGRESS)
                .build();
        incubationRepository.save(incubation);
    }

    public StartIncubationUseCase(
            final IncubationRepository incubationRepository,
            final InventoryRepository inventoryRepository,
            final PlayerRepository playerRepository
    ) {
        this.incubationRepository = incubationRepository;
        this.inventoryRepository = inventoryRepository;
        this.playerRepository = playerRepository;
    }
}
