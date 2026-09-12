package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Rarity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RarityRollerTest {
    @Test
    void collectionMasteryMovesFiveWeightPointsFromCommonToHigherRaritiesOnBirth() {
        assertArrayEquals(new int[]{70, 20, 8, 2}, RarityRoller.weightsFor(0, false));
        assertArrayEquals(new int[]{65, 23, 9, 3}, RarityRoller.weightsFor(0, true));
    }

    @Test
    void collectionMasteryPreservesDynamicRebirthWeightsAndTotal() {
        assertArrayEquals(new int[]{50, 30, 13, 3}, RarityRoller.weightsFor(10, false));
        assertArrayEquals(new int[]{45, 33, 14, 4}, RarityRoller.weightsFor(10, true));
    }

    @Test
    void rarityRollersKeepReturningValidRarities() {
        assertNotNull(RarityRoller.roll(true));
        assertNotNull(RarityRoller.rollForRebirth(Rarity.COMMON, 1, true));
    }
}
