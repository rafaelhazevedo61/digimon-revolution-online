package com.dro.modules.digimon.domain;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Digimon.
 */
public class DigimonLevelRules {

    public static final int MAX_LEVEL = 100;

    public static int xpToNextLevel(int level) {

        if (level >= MAX_LEVEL) {
            return 0;
        }

        return level * 100;
    }

    public static int totalXpToReachLevel(int targetLevel) {

        if (targetLevel <= 1) {
            return 0;
        }

        if (targetLevel > MAX_LEVEL) {
            targetLevel = MAX_LEVEL;
        }

        int total = 0;

        for (int level = 1; level < targetLevel; level++) {
            total += xpToNextLevel(level);
        }

        return total;
    }

    public static List<LevelExperienceData> getExperienceTable() {
        return IntStream.rangeClosed(1, MAX_LEVEL)
                .mapToObj(level -> new LevelExperienceData(
                        level,
                        xpToNextLevel(level),
                        totalXpToReachLevel(level)
                ))
                .toList();
    }

    public record LevelExperienceData(
            int level,
            int xpToNextLevel,
            int totalXpToReachThisLevel
    ) {
    }
}