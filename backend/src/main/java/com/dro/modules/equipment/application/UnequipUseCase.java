package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Equipamentos.
 */
@Service
public class UnequipUseCase {
    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void execute(String token, UUID equipmentId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        var player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        Equipment equipment = equipmentRepository.findById(equipmentId).orElseThrow(() -> new NotFoundException("Equipment not found"));
        if (!playerId.equals(equipment.getPlayerId()) || !digimon.getId().equals(equipment.getDigimonId())) {
            throw new ForbiddenException("Equipment does not belong to this player and active Digimon");
        }
        if (!equipment.isEquipped()) {
            throw new BadRequestException("Equipment is not equipped");
        }
        digimon.clearSlot(equipment.getSlot());
        equipment.unequip();
        equipment.setDigimonId(null);
        digimonRepository.save(digimon);
        equipmentRepository.save(equipment);
    }

    public UnequipUseCase(final EquipmentRepository equipmentRepository, final DigimonRepository digimonRepository, final PlayerRepository playerRepository) {
        this.equipmentRepository = equipmentRepository;
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
    }
}
