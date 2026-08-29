package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentRarityRules;
import com.dro.modules.equipment.domain.EquipmentTemplate;
import com.dro.modules.equipment.domain.EquipmentTemplateMapper;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

/** Concede equipamentos ao inventário global do jogador. */
@Service
public class GrantEquipmentUseCase {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final DigimonRepository digimonRepository;

    public UUID execute(UUID digimonId, String templateName) { return execute(digimonId, templateName, null); }

    public UUID execute(UUID digimonId, String templateName, EquipmentRarity rarityOverride) {
        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));
        EquipmentTemplate template = equipmentTemplateRepository.findByName(templateName)
                .map(EquipmentTemplateMapper::toTemplate)
                .orElseThrow(() -> new NotFoundException("Equipment template not found: " + templateName));
        EquipmentRarity rarity = rarityOverride != null ? rarityOverride
                : (template.getRarity() != null ? template.getRarity() : EquipmentRarityRules.rollRarity());
        Equipment equipment = Equipment.builder().id(UUID.randomUUID()).playerId(digimon.getPlayerId())
                .digimonId(null).name(template.getName()).slot(template.getSlot()).rarity(rarity)
                .setCode(template.getSetCode()).tier(template.getTier()).bonusHp(template.getBonusHp())
                .bonusAttack(template.getBonusAttack()).bonusDefense(template.getBonusDefense())
                .createdAt(LocalDateTime.now()).build();
        equipmentRepository.save(equipment);
        return equipment.getId();
    }

    @Autowired
    public GrantEquipmentUseCase(final EquipmentRepository equipmentRepository,
                                 final EquipmentTemplateRepository equipmentTemplateRepository,
                                 final DigimonRepository digimonRepository) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentTemplateRepository = equipmentTemplateRepository;
        this.digimonRepository = digimonRepository;
    }

    /** Compatibilidade para testes antigos; produção deve fornecer DigimonRepository. */
    public GrantEquipmentUseCase(final EquipmentRepository equipmentRepository,
                                 final EquipmentTemplateRepository equipmentTemplateRepository) {
        this(equipmentRepository, equipmentTemplateRepository, null);
    }
}
