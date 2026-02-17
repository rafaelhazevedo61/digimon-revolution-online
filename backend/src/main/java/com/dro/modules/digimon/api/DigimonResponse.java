package com.dro.modules.digimon.api;

import java.time.LocalDateTime;
import java.util.UUID;

public record DigimonResponse(
        UUID id,
        String name,
        String type,
        String stage,
        int level,
        int experience,
        int hp,
        int attack,
        int defense,
        int ivHp,
        int ivAttack,
        int ivDefense,
        LocalDateTime createdAt
) {}
