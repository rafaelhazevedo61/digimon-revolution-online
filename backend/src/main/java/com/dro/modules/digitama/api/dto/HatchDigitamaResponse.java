package com.dro.modules.digitama.api.dto;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;

import java.util.UUID;

public record HatchDigitamaResponse(
        UUID id,
        String name,
        String type,
        Stage stage,
        int level,
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
        int maxEnergy
) {
    public static HatchDigitamaResponse from(Digimon digimon) {
        return new HatchDigitamaResponse(
                digimon.getId(),
                digimon.getName(),
                digimon.getType(),
                digimon.getStage(),
                digimon.getLevel(),
                digimon.getHp(),
                digimon.getAttack(),
                digimon.getDefense(),
                digimon.getIvHp(),
                digimon.getIvAttack(),
                digimon.getIvDefense(),
                digimon.getRarity(),
                digimon.getPersonality(),
                digimon.getTrait(),
                digimon.getEnergy(),
                digimon.getMaxEnergy()
        );
    }
}
