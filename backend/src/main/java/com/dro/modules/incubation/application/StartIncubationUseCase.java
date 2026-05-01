package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.*;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartIncubationUseCase {

    private final IncubationRepository incubationRepository;
    private final InventoryRepository inventoryRepository;
    private final PlayerRepository playerRepository;

    public void execute(String token, ItemType digitamaType, ItemType incubatorType) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        // 1️⃣ Verificar incubação ativa
        incubationRepository.findByPlayerIdAndStatus(playerId, IncubationStatus.IN_PROGRESS)
                .ifPresent(i -> {
                    throw new RuntimeException("Incubation already in progress");
                });

        // 2️⃣ Validar tipos
        if (!DigitamaRules.isDigitama(digitamaType)) {
            throw new RuntimeException("Invalid digitama");
        }

        // 3️⃣ Buscar digimon ativo
        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new RuntimeException("No active digimon selected");
        }

        UUID digimonId = player.getActiveDigimonId();

        // 4️⃣ Validar inventário - Digitama
        InventoryItem digitamaItem = inventoryRepository
                .findByDigimonIdAndItemType(digimonId, digitamaType)
                .orElseThrow(() -> new RuntimeException("Digitama not found"));

        if (digitamaItem.getQuantity() <= 0) {
            throw new RuntimeException("No digitama available");
        }

        // 5️⃣ Validar inventário - Incubadora
        InventoryItem incubatorItem = inventoryRepository
                .findByDigimonIdAndItemType(digimonId, incubatorType)
                .orElseThrow(() -> new RuntimeException("Incubator not found"));

        if (incubatorItem.getQuantity() <= 0) {
            throw new RuntimeException("No incubator available");
        }

        // 6️⃣ Consumir itens
        digitamaItem.setQuantity(digitamaItem.getQuantity() - 1);
        incubatorItem.setQuantity(incubatorItem.getQuantity() - 1);

        inventoryRepository.save(digitamaItem);
        inventoryRepository.save(incubatorItem);

        // 7️⃣ Calcular tempo
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finishAt = now.plus(IncubatorRules.getIncubationTime(incubatorType));

        // 8️⃣ Criar incubação
        Incubation incubation = Incubation.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .digitamaType(digitamaType)
                .incubatorType(incubatorType)
                .startedAt(now)
                .finishAt(finishAt)
                .status(IncubationStatus.IN_PROGRESS)
                .build();

        incubationRepository.save(incubation);
    }
}
