package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.request.CreateMissionRequest;
import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.domain.*;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreateMissionUseCase {

    private final MissionDefinitionRepository missionDefinitionRepository;

    @Transactional
    public AdminMissionResponse execute(CreateMissionRequest request) {

        if (missionDefinitionRepository.existsById(request.id())) {
            throw new ConflictException("Mission already exists: " + request.id());
        }

        validateLootConsistency(request);

        LocalDateTime now = LocalDateTime.now();

        MissionDefinitionEntity entity = MissionDefinitionEntity.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .area(request.area())
                .requiredStage(request.requiredStage())
                .requiredLevel(request.requiredLevel())
                .baseXp(request.baseXp())
                .energyCost(request.energyCost())
                .durationSeconds(request.durationSeconds())
                .createdAt(now)
                .updatedAt(now)
                .createdBy("admin")
                .updatedBy("admin")
                .newEntity(true)
                .build();

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

        try {
            missionDefinitionRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Mission already exists: " + request.id());
        }

        return AdminMissionResponse.from(entity);
    }

    private void validateLootConsistency(CreateMissionRequest request) {
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
