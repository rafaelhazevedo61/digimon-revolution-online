package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.request.CreateMissionRequest;
import com.dro.modules.mission.api.dto.request.LootChanceRequest;
import com.dro.modules.mission.api.dto.request.LootItemRequest;
import com.dro.modules.mission.api.dto.request.RewardRequest;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.domain.*;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
public class CreateMissionUseCase {
    private final MissionDefinitionRepository missionDefinitionRepository;
    private final ChestDefinitionRepository chestDefinitionRepository;

    @Transactional
    public AdminMissionResponse execute(CreateMissionRequest request) {
        if (missionDefinitionRepository.existsById(request.id())) {
            throw new ConflictException("Mission already exists: " + request.id());
        }
        validateChestConfiguration(request);
        ChestDefinitionEntity chest = resolveActiveChest(request.chestCode());
        LocalDateTime now = LocalDateTime.now();
        MissionDefinitionEntity entity = MissionDefinitionEntity.builder().id(request.id()).name(request.name()).description(request.description()).area(request.area()).requiredStage(request.requiredStage()).requiredLevel(request.requiredLevel()).baseXp(request.baseXp()).baseBits(request.baseBits()).energyCost(request.energyCost()).durationSeconds(request.durationSeconds()).chestDefinition(chest).createdAt(now).updatedAt(now).createdBy("admin").updatedBy("admin").newEntity(true).build();
        if (request.rewards() != null && !request.rewards().isEmpty()) {
            request.rewards().forEach(r -> {
                MissionRewardEntity reward = MissionRewardEntity.builder().mission(entity).itemType(r.itemType()).baseQuantity(r.baseQuantity()).build();
                entity.getRewards().add(reward);
            });
        }
        if (request.lootChances() != null && !request.lootChances().isEmpty()) {
            request.lootChances().forEach(c -> {
                MissionLootChanceEntity chance = MissionLootChanceEntity.builder().mission(entity).rarity(c.rarity()).chance(c.chance()).build();
                entity.getLootChances().add(chance);
            });
        }
        if (request.lootItems() != null && !request.lootItems().isEmpty()) {
            request.lootItems().forEach(i -> {
                MissionLootItemEntity item = MissionLootItemEntity.builder().mission(entity).rarity(i.rarity()).itemType(i.itemType()).quantity(i.quantity()).build();
                entity.getLootItems().add(item);
            });
        }
        try {
            missionDefinitionRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Mission already exists: " + request.id());
        }
        return AdminMissionResponse.from(entity);
    }

    private void validateChestConfiguration(CreateMissionRequest request) {
        if (request.chestCode() == null || request.chestCode().isBlank()) {
            throw new BadRequestException("chestCode é obrigatório para uma missão do novo sistema de loot.");
        }
        if (hasLegacyLoot(request.rewards(), request.lootChances(), request.lootItems())) {
            throw new BadRequestException("Missões com Baú da Área não aceitam recompensas fixas ou loot legado; configure a Loot Table do baú.");
        }
    }

    private boolean hasLegacyLoot(java.util.List<RewardRequest> rewards, java.util.List<LootChanceRequest> lootChances, java.util.List<LootItemRequest> lootItems) {
        return (rewards != null && !rewards.isEmpty()) || (lootChances != null && !lootChances.isEmpty()) || (lootItems != null && !lootItems.isEmpty());
    }

    private ChestDefinitionEntity resolveActiveChest(String code) {
        String normalizedCode = code.trim();
        return chestDefinitionRepository.findByCodeAndActiveTrue(normalizedCode).orElseThrow(() -> new NotFoundException("Baú da Área ativo não encontrado: " + normalizedCode));
    }

    public CreateMissionUseCase(final MissionDefinitionRepository missionDefinitionRepository, final ChestDefinitionRepository chestDefinitionRepository) {
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.chestDefinitionRepository = chestDefinitionRepository;
    }
}
