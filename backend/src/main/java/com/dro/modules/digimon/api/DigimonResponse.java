package com.dro.modules.digimon.api;

import com.dro.modules.digimon.domain.Stage;

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
        boolean active,
        LocalDateTime createdAt
) {}