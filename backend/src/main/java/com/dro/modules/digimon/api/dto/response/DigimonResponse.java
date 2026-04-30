package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.enums.*;

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
        Trait trait,
        int energy,
        int maxEnergy,
        int bits,
        int rebirthCount,
        UUID rebornedFrom,
        DigimonStatus status,
        int equipBonusHp,
        int equipBonusAttack,
        int equipBonusDefense
) {
}