package com.dro.modules.mission.api.dto.response;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonLevelRules;

import java.util.UUID;

/**
 * Progresso de experiência de um Digimon após o resgate de uma missão.
 */
public record MissionDigimonExperienceResponse(
        UUID id,
        String name,
        String stage,
        String imageUrl,
        int level,
        int levelsGained,
        int experience,
        int experienceToNextLevel,
        double experiencePercent
) {
    public static MissionDigimonExperienceResponse from(Digimon digimon, String imageUrl, int previousLevel) {
        int experienceToNextLevel = digimon.getExperienceToNextLevel();
        double experiencePercent = experienceToNextLevel <= 0
                ? 100.0
                : Math.min(100.0, (digimon.getExperience() * 100.0) / experienceToNextLevel);

        return new MissionDigimonExperienceResponse(
                digimon.getId(),
                digimon.getName(),
                digimon.getStage() != null ? digimon.getStage().name() : null,
                imageUrl,
                digimon.getLevel(),
                Math.max(0, digimon.getLevel() - previousLevel),
                digimon.getExperience(),
                experienceToNextLevel,
                experiencePercent
        );
    }
}
