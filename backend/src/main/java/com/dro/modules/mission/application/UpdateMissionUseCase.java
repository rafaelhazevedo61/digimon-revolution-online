package com.dro.modules.mission.application;

import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.mission.api.dto.request.UpdateMissionRequest;
import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.domain.*;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
public class UpdateMissionUseCase {
    private final MissionDefinitionRepository missionDefinitionRepository;
    private final ChestDefinitionRepository chestDefinitionRepository;

    @Transactional
    public AdminMissionResponse execute(String id, UpdateMissionRequest request) {
        MissionDefinitionEntity entity = missionDefinitionRepository.findById(id).orElseThrow(() -> new NotFoundException("Mission not found: " + id));
        validateChestConfiguration(request);
        ChestDefinitionEntity chest = resolveActiveChest(request.chestCode());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setArea(request.area());
        entity.setRequiredStage(request.requiredStage());
        entity.setRequiredLevel(request.requiredLevel());
        entity.setBaseXp(request.baseXp());
        entity.setBaseBits(request.baseBits());
        entity.setEnergyCost(request.energyCost());
        entity.setDurationSeconds(request.durationSeconds());
        entity.setChestDefinition(chest);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");
        entity.getRewards().clear();
        if (request.rewards() != null) {
            request.rewards().forEach(r -> {
                MissionRewardEntity reward = MissionRewardEntity.builder().mission(entity).itemType(r.itemType()).baseQuantity(r.baseQuantity()).build();
                entity.getRewards().add(reward);
            });
        }
        entity.getLootChances().clear();
        if (request.lootChances() != null) {
            request.lootChances().forEach(c -> {
                MissionLootChanceEntity chance = MissionLootChanceEntity.builder().mission(entity).rarity(c.rarity()).chance(c.chance()).build();
                entity.getLootChances().add(chance);
            });
        }
        entity.getLootItems().clear();
        if (request.lootItems() != null) {
            request.lootItems().forEach(i -> {
                MissionLootItemEntity item = MissionLootItemEntity.builder().mission(entity).rarity(i.rarity()).itemType(i.itemType()).quantity(i.quantity()).build();
                entity.getLootItems().add(item);
            });
        }
        missionDefinitionRepository.saveAndFlush(entity);
        return AdminMissionResponse.from(entity);
    }

    private void validateChestConfiguration(UpdateMissionRequest request) {
        if (request.chestCode() == null || request.chestCode().isBlank()) {
            throw new BadRequestException("chestCode é obrigatório para uma missão do novo sistema de loot.");
        }
        if (hasLegacyLoot(request.rewards(), request.lootChances(), request.lootItems())) {
            throw new BadRequestException("Missões com Baú da Área não aceitam recompensas fixas ou loot legado; configure a Loot Table do baú.");
        }
    }

    private boolean hasLegacyLoot(java.util.List<com.dro.modules.mission.api.dto.request.RewardRequest> rewards, java.util.List<com.dro.modules.mission.api.dto.request.LootChanceRequest> lootChances, java.util.List<com.dro.modules.mission.api.dto.request.LootItemRequest> lootItems) {
        return (rewards != null && !rewards.isEmpty()) || (lootChances != null && !lootChances.isEmpty()) || (lootItems != null && !lootItems.isEmpty());
    }

    private ChestDefinitionEntity resolveActiveChest(String code) {
        String normalizedCode = code.trim();
        return chestDefinitionRepository.findByCodeAndActiveTrue(normalizedCode).orElseThrow(() -> new NotFoundException("Baú da Área ativo não encontrado: " + normalizedCode));
    }

    public UpdateMissionUseCase(final MissionDefinitionRepository missionDefinitionRepository, final ChestDefinitionRepository chestDefinitionRepository) {
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.chestDefinitionRepository = chestDefinitionRepository;
    }
}
