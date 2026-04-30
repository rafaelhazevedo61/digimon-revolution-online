package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;

import java.time.LocalDateTime;
import java.util.UUID;

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
        int rebirthCount,
        DigimonStatus status,
        UUID rebornedFrom,
        LocalDateTime createdAt
) {
}