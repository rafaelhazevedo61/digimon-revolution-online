package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.CreateEquipmentTemplateRequest;
import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.domain.EquipmentTemplateEntity;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Equipamentos.
 */
@Service
@RequiredArgsConstructor
public class CreateEquipmentTemplateUseCase {

    private final EquipmentTemplateRepository equipmentTemplateRepository;

    @CacheEvict(cacheNames = "equipmentTemplates", allEntries = true)
    @Transactional
    public EquipmentTemplateResponse execute(CreateEquipmentTemplateRequest request) {

        if (equipmentTemplateRepository.findByName(request.name()).isPresent()) {
            throw new ConflictException("Equipment template already exists: " + request.name());
        }

        LocalDateTime now = LocalDateTime.now();

        EquipmentTemplateEntity entity = EquipmentTemplateEntity.builder()
                .name(request.name())
                .setCode(request.setCode())
                .tier(request.tier())
                .slot(request.slot())
                .rarity(request.rarity())
                .bonusHp(request.bonusHp())
                .bonusAttack(request.bonusAttack())
                .bonusDefense(request.bonusDefense())
                .createdAt(now)
                .updatedAt(now)
                .createdBy("admin")
                .updatedBy("admin")
                .newEntity(true)
                .build();

        try {
            equipmentTemplateRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Equipment template already exists: " + request.name());
        }

        return EquipmentTemplateResponse.from(entity);
    }
}
