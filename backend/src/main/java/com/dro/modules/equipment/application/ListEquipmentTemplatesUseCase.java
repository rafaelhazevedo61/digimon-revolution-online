package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Equipamentos.
 */
@Service
@RequiredArgsConstructor
public class ListEquipmentTemplatesUseCase {

    private final EquipmentTemplateRepository equipmentTemplateRepository;

    @Cacheable(cacheNames = "equipmentTemplates", key = "#activeOnly == null ? 'all' : #activeOnly.toString()")
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
