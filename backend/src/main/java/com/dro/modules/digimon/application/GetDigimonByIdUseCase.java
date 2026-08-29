package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digimon.
 */
@Service
public class GetDigimonByIdUseCase {
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final EquipmentRepository equipmentRepository;

    public DigimonResponse execute(String token, UUID digimonId) {
        TokenExtractor.extractPlayerId(token);
        Digimon digimon = digimonRepository.findById(digimonId).orElseThrow(() -> new NotFoundException("Digimon not found"));
        List<Equipment> equipped = getEquippedItems(digimon);
        DigimonInfos info = digimon.getDigimonInfoId() != null ? digimonInfosRepository.findById(digimon.getDigimonInfoId()).orElse(null) : null;
        return new DigimonResponse(digimon.getId(), digimon.getName(), digimon.getType(), digimon.getStage(), digimon.getLevel(), digimon.getExperience(), digimon.getHp(), digimon.getAttack(), digimon.getDefense(), digimon.getIvHp(), digimon.getIvAttack(), digimon.getIvDefense(), digimon.getGrade(), digimon.getRarity(), digimon.getPersonality(), digimon.getTrait(), digimon.getEnergy(), digimon.getMaxEnergy(), digimon.getBits(), digimon.getRebirthCount(), digimon.getRebornedFrom(), digimon.getStatus(), digimon.getDigimonInfoId(), info != null ? info.getAttribute().name() : null, info != null ? info.getElement().name() : null, info != null ? info.getImageUrl() : null, EquipmentRules.totalBonusHp(equipped), EquipmentRules.totalBonusAttack(equipped), EquipmentRules.totalBonusDefense(equipped), 0, 0, 0, 0, digimon.isRarityChangedByDie(), digimon.getOriginalRarityBeforeDie(), digimon.getRarityChangedByDieAt());
    }

    private List<Equipment> getEquippedItems(Digimon digimon) {
        List<Equipment> equipped = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            UUID equipId = digimon.getEquipmentIdBySlot(slot);
            if (equipId != null) {
                equipmentRepository.findById(equipId).ifPresent(equipped::add);
            }
        }
        return equipped;
    }

    public GetDigimonByIdUseCase(final DigimonRepository digimonRepository, final DigimonInfosRepository digimonInfosRepository, final EquipmentRepository equipmentRepository) {
        this.digimonRepository = digimonRepository;
        this.digimonInfosRepository = digimonInfosRepository;
        this.equipmentRepository = equipmentRepository;
    }
}
