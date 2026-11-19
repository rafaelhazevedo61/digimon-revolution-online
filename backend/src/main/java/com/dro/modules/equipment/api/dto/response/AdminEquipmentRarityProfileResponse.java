package com.dro.modules.equipment.api.dto.response;

import com.dro.modules.equipment.domain.EquipmentRarityProfileEntity;

import java.time.LocalDateTime;

/** Visão administrativa de um perfil de raridade de equipamento. */
public record AdminEquipmentRarityProfileResponse(
        String profileKey,
        String displayName,
        int commonPercent,
        int rarePercent,
        int epicPercent,
        int legendaryPercent,
        LocalDateTime updatedAt,
        String updatedBy
) {

    /** Constrói a resposta a partir do perfil persistido. */
    public static AdminEquipmentRarityProfileResponse from(EquipmentRarityProfileEntity entity) {
        return new AdminEquipmentRarityProfileResponse(
                entity.getProfileKey(),
                entity.getDisplayName(),
                entity.getCommonPercent(),
                entity.getRarePercent(),
                entity.getEpicPercent(),
                entity.getLegendaryPercent(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
