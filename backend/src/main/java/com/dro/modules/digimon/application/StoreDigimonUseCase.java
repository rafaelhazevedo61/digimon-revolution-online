package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreDigimonUseCase {

    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final EquipmentRepository equipmentRepository;

    public Digimon execute(String token, UUID digimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Digimon does not belong to player");
        }

        if (digimon.getStatus() != DigimonStatus.ACTIVE) {
            throw new BadRequestException("Only active Digimons can be stored");
        }

        if (digimon.getId().equals(player.getActiveDigimonId())) {
            throw new BadRequestException("Nao e possivel guardar o Digimon ativo. Troque de Digimon primeiro.");
        }

        long storedCount = digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.STORED);
        if (storedCount >= player.getMaxStorageSlots()) {
            throw new BadRequestException(
                    "Storage cheio (" + storedCount + "/" + player.getMaxStorageSlots() + ").");
        }

        unequipAll(digimon);

        digimon.setStatus(DigimonStatus.STORED);
        digimonRepository.save(digimon);

        return digimon;
    }

    private void unequipAll(Digimon digimon) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            UUID equipId = digimon.getEquipmentIdBySlot(slot);
            if (equipId != null) {
                Equipment equipment = equipmentRepository.findById(equipId).orElse(null);
                if (equipment != null) {
                    equipment.unequip();
                    equipmentRepository.save(equipment);
                }
                digimon.clearSlot(slot);
            }
        }
    }
}
