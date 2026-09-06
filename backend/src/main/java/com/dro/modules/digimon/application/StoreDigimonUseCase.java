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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digimon.
 */
@Service
public class StoreDigimonUseCase {
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public Digimon execute(String token, UUID digimonId) {
        return executeForPlayer(TokenExtractor.extractPlayerId(token), digimonId);
    }

    @Transactional
    public Digimon executeForPlayer(UUID playerId, UUID digimonId) {
        Player player = playerRepository.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon digimon = digimonRepository.findByIdForUpdate(digimonId).orElseThrow(() -> new NotFoundException("Digimon not found"));
        if (!digimon.getPlayerId().equals(playerId)) {
            throw new BadRequestException("Digimon does not belong to player");
        }
        if (digimon.getStatus() == DigimonStatus.HATCHED) {
            long storedCount = digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.STORED);
            if (storedCount >= player.getMaxStorageSlots()) {
                throw new BadRequestException("Storage cheio (" + storedCount + "/" + player.getMaxStorageSlots() + ").");
            }
            digimon.setStatus(DigimonStatus.STORED);
            digimonRepository.save(digimon);
            return digimon;
        }
        if (digimon.getStatus() != DigimonStatus.ACTIVE) {
            throw new BadRequestException("Only active or newly hatched Digimons can be stored");
        }
        if (digimon.getId().equals(player.getActiveDigimonId())) {
            throw new BadRequestException("Nao e possivel guardar o Digimon ativo. Troque de Digimon primeiro.");
        }
        long storedCount = digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.STORED);
        if (storedCount >= player.getMaxStorageSlots()) {
            throw new BadRequestException("Storage cheio (" + storedCount + "/" + player.getMaxStorageSlots() + ").");
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

    public StoreDigimonUseCase(final DigimonRepository digimonRepository, final PlayerRepository playerRepository, final EquipmentRepository equipmentRepository) {
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.equipmentRepository = equipmentRepository;
    }
}
