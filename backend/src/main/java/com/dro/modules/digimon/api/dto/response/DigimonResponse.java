package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;

import java.time.LocalDateTime;
import java.util.UUID;

public record DigimonResponse(
        UUID id,
        String name,
        String type,
        Stage stage,
        int level,
        int experience,
        int hp,
        int attack,
        int defense,
        int ivHp,
        int ivAttack,
        int ivDefense,
        Rarity rarity,
        Personality personality,
        int energy,
        int maxEnergy,
        int bits,
        int rebirthCount,
        UUID rebornedFrom,
        DigimonStatus status
) {
}