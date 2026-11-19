package com.dro.modules.digitama.domain;

import com.dro.shared.exception.BadRequestException;

import java.util.List;
import java.util.Random;

/**
 * Componente da camada de componente de domínio do módulo de Digitama.
 */
public class DigitamaPoolRoller {

    private static final Random random = new Random();

    private DigitamaPoolRoller() {
    }

    public static DigitamaPoolEntry roll(List<DigitamaPoolEntry> entries) {

        List<DigitamaPoolEntry> activeEntries = entries.stream()
                .filter(DigitamaPoolEntry::isActive)
                .filter(entry -> entry.getWeight() > 0)
                .toList();

        if (activeEntries.isEmpty()) {
            throw new BadRequestException("No active digimon available in this digitama pool");
        }

        int totalWeight = activeEntries.stream()
                .mapToInt(DigitamaPoolEntry::getWeight)
                .sum();

        int roll = random.nextInt(totalWeight) + 1;

        int accumulated = 0;

        for (DigitamaPoolEntry entry : activeEntries) {
            accumulated += entry.getWeight();

            if (roll <= accumulated) {
                return entry;
            }
        }

        throw new BadRequestException("Failed to roll digimon from digitama pool");
    }
}