package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record DigimonLineageItemResponse(
        UUID id,
        String name,
        String type,
        Stage stage,
        int level,
        int ivHp,
        int ivAttack,
        int ivDefense,
        Rarity rarity,
        Personality personality,
        Trait trait,
        int rebirthCount,
        DigimonStatus status,
        UUID rebornedFrom,
        LocalDateTime createdAt
) {
}