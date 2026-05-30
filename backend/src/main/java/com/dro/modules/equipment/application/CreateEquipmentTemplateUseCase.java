package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.CreateEquipmentTemplateRequest;
import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.domain.EquipmentTemplateEntity;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateEquipmentTemplateUseCase {

    private final EquipmentTemplateRepository equipmentTemplateRepository;

    public EquipmentTemplateResponse execute(CreateEquipmentTemplateRequest request) {

        if (equipmentTemplateRepository.existsById(request.name())) {
            throw new ConflictException("Equipment template already exists: " + request.name());
        }

        EquipmentTemplateEntity entity = EquipmentTemplateEntity.builder()
                .name(request.name())
                .slot(request.slot())
                .rarity(request.rarity())
                .bonusHp(request.bonusHp())
                .bonusAttack(request.bonusAttack())
                .bonusDefense(request.bonusDefense())
                .build();

        equipmentTemplateRepository.save(entity);

        return EquipmentTemplateResponse.from(entity);
    }
}
