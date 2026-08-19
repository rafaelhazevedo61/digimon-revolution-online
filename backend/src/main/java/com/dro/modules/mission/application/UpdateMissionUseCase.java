package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.request.UpdateMissionRequest;
import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.domain.*;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Missões.
 */
@Service
@RequiredArgsConstructor
public class UpdateMissionUseCase {

    private final MissionDefinitionRepository missionDefinitionRepository;

    @Transactional
    public AdminMissionResponse execute(String id, UpdateMissionRequest request) {

        MissionDefinitionEntity entity = missionDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mission not found: " + id));

        validateLootConsistency(request);

        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setArea(request.area());
        entity.setRequiredStage(request.requiredStage());
        entity.setRequiredLevel(request.requiredLevel());
        entity.setBaseXp(request.baseXp());
        entity.setBaseBits(request.baseBits());
        entity.setEnergyCost(request.energyCost());
        entity.setDurationSeconds(request.durationSeconds());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");

        entity.getRewards().clear();
        if (request.rewards() != null) {
            request.rewards().forEach(r -> {
                MissionRewardEntity reward = MissionRewardEntity.builder()
                        .mission(entity)
                        .itemType(r.itemType())
                        .baseQuantity(r.baseQuantity())
                        .build();
                entity.getRewards().add(reward);
            });
        }

        entity.getLootChances().clear();
        if (request.lootChances() != null) {
            request.lootChances().forEach(c -> {
                MissionLootChanceEntity chance = MissionLootChanceEntity.builder()
                        .mission(entity)
                        .rarity(c.rarity())
                        .chance(c.chance())
                        .build();
                entity.getLootChances().add(chance);
            });
        }

        entity.getLootItems().clear();
        if (request.lootItems() != null) {
            request.lootItems().forEach(i -> {
                MissionLootItemEntity item = MissionLootItemEntity.builder()
                        .mission(entity)
                        .rarity(i.rarity())
                        .itemType(i.itemType())
                        .quantity(i.quantity())
                        .build();
                entity.getLootItems().add(item);
            });
        }

        missionDefinitionRepository.saveAndFlush(entity);

        return AdminMissionResponse.from(entity);
    }

    private void validateLootConsistency(UpdateMissionRequest request) {
        if (request.lootChances() == null || request.lootChances().isEmpty()) {
            return;
        }

        if (request.lootItems() == null || request.lootItems().isEmpty()) {
            throw new BadRequestException("lootItems is required when lootChances is defined");
        }

        Set<String> chancesRarities = new HashSet<>();
        request.lootChances().forEach(c -> chancesRarities.add(c.rarity().name()));

        Set<String> itemsRarities = new HashSet<>();
        request.lootItems().forEach(i -> itemsRarities.add(i.rarity().name()));

        for (String rarity : chancesRarities) {
            if (!itemsRarities.contains(rarity)) {
                throw new BadRequestException(
                        "lootItems must have at least one item for rarity: " + rarity);
            }
        }
    }
}
