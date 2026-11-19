package com.dro.modules.digimon.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionMasteryMultiplierTest {
    @Test
    void collectionMasteryProvidesFivePercentStatusMultiplier() {
        assertEquals(1.0, RebirthRules.calculateCollectionMasteryMultiplier(false));
        assertEquals(1.05, RebirthRules.calculateCollectionMasteryMultiplier(true));
    }
}
