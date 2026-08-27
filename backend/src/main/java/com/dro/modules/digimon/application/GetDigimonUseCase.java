package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digimon.
 */
@Service
public class GetDigimonUseCase {
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final EquipmentRepository equipmentRepository;

    public List<DigimonResponse> execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        var digimons = digimonRepository.findByPlayerId(playerId);
        return digimons.stream()
                .filter(d -> d.getStatus() != DigimonStatus.REBORN)
                .sorted(Comparator.comparingInt(d -> switch (d.getStatus()) {
                    case ACTIVE -> 0;
                    case HATCHED -> 1;
                    case STORED -> 2;
                    case REBORN -> 3;
                }))
                .map(d -> {
            List<Equipment> equipped = getEquippedItems(d);
            DigimonInfos info = d.getDigimonInfoId() != null ? digimonInfosRepository.findById(d.getDigimonInfoId()).orElse(null) : null;
            return new DigimonResponse(d.getId(), d.getName(), d.getType(), d.getStage(), d.getLevel(), d.getExperience(), d.getHp(), d.getAttack(), d.getDefense(), d.getIvHp(), d.getIvAttack(), d.getIvDefense(), d.getGrade(), d.getRarity(), d.getPersonality(), d.getTrait(), d.getEnergy(), d.getMaxEnergy(), d.getBits(), d.getRebirthCount(), d.getRebornedFrom(), d.getStatus(), d.getDigimonInfoId(), info != null ? info.getAttribute().name() : null, info != null ? info.getElement().name() : null, info != null ? info.getImageUrl() : null, EquipmentRules.totalBonusHp(equipped), EquipmentRules.totalBonusAttack(equipped), EquipmentRules.totalBonusDefense(equipped), 0, 0, 0, 0);
        }).toList();
    }

    public List<DigimonResponse> executeStorage(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        return digimonRepository.findByPlayerIdAndStatus(playerId, DigimonStatus.STORED).stream().map(d -> {
            DigimonInfos info = d.getDigimonInfoId() != null ? digimonInfosRepository.findById(d.getDigimonInfoId()).orElse(null) : null;
            return new DigimonResponse(d.getId(), d.getName(), d.getType(), d.getStage(), d.getLevel(), d.getExperience(), d.getHp(), d.getAttack(), d.getDefense(), d.getIvHp(), d.getIvAttack(), d.getIvDefense(), d.getGrade(), d.getRarity(), d.getPersonality(), d.getTrait(), d.getEnergy(), d.getMaxEnergy(), d.getBits(), d.getRebirthCount(), d.getRebornedFrom(), d.getStatus(), d.getDigimonInfoId(), info != null ? info.getAttribute().name() : null, info != null ? info.getElement().name() : null, info != null ? info.getImageUrl() : null, 0, 0, 0, 0, 0, 0, 0);
        }).toList();
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

    public GetDigimonUseCase(final DigimonRepository digimonRepository, final DigimonInfosRepository digimonInfosRepository, final EquipmentRepository equipmentRepository) {
        this.digimonRepository = digimonRepository;
        this.digimonInfosRepository = digimonInfosRepository;
        this.equipmentRepository = equipmentRepository;
    }
}
