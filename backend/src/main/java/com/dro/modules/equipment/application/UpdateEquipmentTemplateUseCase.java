package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.UpdateEquipmentTemplateRequest;
import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.domain.EquipmentTemplateEntity;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateEquipmentTemplateUseCase {

    private final EquipmentTemplateRepository equipmentTemplateRepository;

    @Transactional
    public EquipmentTemplateResponse execute(String name, UpdateEquipmentTemplateRequest request) {

        EquipmentTemplateEntity entity = equipmentTemplateRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Equipment template not found: " + name));

        entity.setSlot(request.slot());
        if (request.setCode() != null) entity.setSetCode(request.setCode());
        if (request.tier() != null) entity.setTier(request.tier());
        if (request.rarity() != null) entity.setRarity(request.rarity());
        entity.setBonusHp(request.bonusHp());
        entity.setBonusAttack(request.bonusAttack());
        entity.setBonusDefense(request.bonusDefense());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");

        equipmentTemplateRepository.save(entity);

        return EquipmentTemplateResponse.from(entity);
    }
}
