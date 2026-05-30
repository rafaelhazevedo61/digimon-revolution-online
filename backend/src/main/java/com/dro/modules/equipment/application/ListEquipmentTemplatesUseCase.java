package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListEquipmentTemplatesUseCase {

    private final EquipmentTemplateRepository equipmentTemplateRepository;

    public List<EquipmentTemplateResponse> execute(Boolean activeOnly) {

        if (Boolean.TRUE.equals(activeOnly)) {
            return equipmentTemplateRepository.findByActiveTrueOrderByNameAsc()
                    .stream()
                    .map(EquipmentTemplateResponse::from)
                    .toList();
        }

        return equipmentTemplateRepository.findAllByOrderByNameAsc()
                .stream()
                .map(EquipmentTemplateResponse::from)
                .toList();
    }
}
