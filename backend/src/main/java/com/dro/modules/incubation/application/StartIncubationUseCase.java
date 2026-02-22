package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.*;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartIncubationUseCase {

    private final IncubationRepository incubationRepository;
    private final InventoryRepository inventoryRepository;

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

        // 3️⃣ Validar inventário - Digitama
        InventoryItem digitamaItem = inventoryRepository
                .findByPlayerIdAndItemType(playerId, digitamaType)
                .orElseThrow(() -> new RuntimeException("Digitama not found"));

        if (digitamaItem.getQuantity() <= 0) {
            throw new RuntimeException("No digitama available");
        }

        // 4️⃣ Validar inventário - Incubadora
        InventoryItem incubatorItem = inventoryRepository
                .findByPlayerIdAndItemType(playerId, incubatorType)
                .orElseThrow(() -> new RuntimeException("Incubator not found"));

        if (incubatorItem.getQuantity() <= 0) {
            throw new RuntimeException("No incubator available");
        }

        // 5️⃣ Consumir itens
        digitamaItem.setQuantity(digitamaItem.getQuantity() - 1);
        incubatorItem.setQuantity(incubatorItem.getQuantity() - 1);

        inventoryRepository.save(digitamaItem);
        inventoryRepository.save(incubatorItem);

        // 6️⃣ Calcular tempo
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finishAt = now.plus(IncubatorRules.getIncubationTime(incubatorType));

        // 7️⃣ Criar incubação
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
