package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Digimon.
 */
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
        DigimonGrade grade,
        Rarity rarity,
        Personality personality,
        Trait trait,
        int energy,
        int maxEnergy,
        int bits,
        int rebirthCount,
        UUID rebornedFrom,
        DigimonStatus status,
        Long digimonInfoId,
        String attribute,
        String element,
        int equipBonusHp,
        int equipBonusAttack,
        int equipBonusDefense,
        int clanBonusHp,
        int clanBonusAttack,
        int clanBonusDefense,
        int clanBonusMaxEnergy
) {

    public static DigimonResponse from(com.dro.modules.digimon.domain.Digimon d) {
        return new DigimonResponse(
                d.getId(), d.getName(), d.getType(), d.getStage(),
                d.getLevel(), d.getExperience(),
                d.getHp(), d.getAttack(), d.getDefense(),
                d.getIvHp(), d.getIvAttack(), d.getIvDefense(),
                d.getGrade(), d.getRarity(), d.getPersonality(), d.getTrait(),
                d.getEnergy(), d.getMaxEnergy(), d.getBits(),
                d.getRebirthCount(), d.getRebornedFrom(), d.getStatus(),
                d.getDigimonInfoId(), null, null, 0, 0, 0, 0, 0, 0, 0
        );
    }
}