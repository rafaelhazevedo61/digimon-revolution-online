package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.domain.EquipmentTemplateEntity;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Equipamentos.
 */
@Service
@RequiredArgsConstructor
public class ToggleEquipmentTemplateUseCase {

    private final EquipmentTemplateRepository equipmentTemplateRepository;

    @Transactional
    public EquipmentTemplateResponse execute(String name) {

        EquipmentTemplateEntity entity = equipmentTemplateRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Equipment template not found: " + name));

        entity.setActive(!entity.isActive());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");

        equipmentTemplateRepository.save(entity);

        return EquipmentTemplateResponse.from(entity);
    }
}
