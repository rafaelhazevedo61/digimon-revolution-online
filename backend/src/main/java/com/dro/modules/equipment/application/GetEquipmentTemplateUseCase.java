package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetEquipmentTemplateUseCase {

    private final EquipmentTemplateRepository equipmentTemplateRepository;

    public EquipmentTemplateResponse execute(String name) {
        return equipmentTemplateRepository.findByName(name)
                .map(EquipmentTemplateResponse::from)
                .orElseThrow(() -> new NotFoundException("Equipment template not found: " + name));
    }
}
