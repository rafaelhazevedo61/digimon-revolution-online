package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.modules.tutorial.domain.TutorialStep;
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
public class EquipUseCase {
    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final TutorialService tutorialService;

    @Transactional
    public void execute(String token, UUID equipmentId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        var player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        Equipment equipment = equipmentRepository.findById(equipmentId).orElseThrow(() -> new NotFoundException("Equipment not found"));
        if (!playerId.equals(equipment.getPlayerId())) {
            throw new ForbiddenException("Equipment does not belong to this player");
        }
        if (equipment.isEquipped() && !digimon.getId().equals(equipment.getDigimonId())) {
            throw new BadRequestException("Equipment is already equipped by another Digimon");
        }
        EquipmentRules.validateEquip(equipment);
        EquipmentRules.validateAscensionEquipRequirement(equipment, digimon);
        UUID currentEquipmentId = digimon.getEquipmentIdBySlot(equipment.getSlot());
        if (currentEquipmentId != null) {
            Equipment currentEquipment = equipmentRepository.findById(currentEquipmentId).orElse(null);
            if (currentEquipment != null) {
                currentEquipment.unequip();
                currentEquipment.setDigimonId(null);
                equipmentRepository.save(currentEquipment);
            }
        }
        equipment.equip();
        equipment.setDigimonId(digimon.getId());
        digimon.setEquipmentBySlot(equipment.getSlot(), equipment.getId());
        equipmentRepository.save(equipment);
        digimonRepository.save(digimon);
        tutorialService.completeStep(playerId, TutorialStep.EQUIP_ITEM);
    }

    public EquipUseCase(final EquipmentRepository equipmentRepository, final DigimonRepository digimonRepository, final PlayerRepository playerRepository, final TutorialService tutorialService) {
        this.equipmentRepository = equipmentRepository;
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.tutorialService = tutorialService;
    }
}
