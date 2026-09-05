package com.dro.modules.equipment.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EquipmentEnhancementRulesTest {
    @Test
    void advancesUntilTierTen() {
        assertEquals(2, EquipmentEnhancementRules.nextTier(1));
        assertEquals(10, EquipmentEnhancementRules.nextTier(9));
        assertEquals(EquipmentEnhancementRules.BASIC_CORE, EquipmentEnhancementRules.requiredCoreCode(2));
        assertEquals(EquipmentEnhancementRules.BASIC_CORE, EquipmentEnhancementRules.requiredCoreCode(4));
        assertEquals(EquipmentEnhancementRules.ADVANCED_CORE, EquipmentEnhancementRules.requiredCoreCode(5));
        assertEquals(EquipmentEnhancementRules.ADVANCED_CORE, EquipmentEnhancementRules.requiredCoreCode(7));
        assertEquals(EquipmentEnhancementRules.SUPREME_CORE, EquipmentEnhancementRules.requiredCoreCode(8));
        assertEquals(EquipmentEnhancementRules.SUPREME_CORE, EquipmentEnhancementRules.requiredCoreCode(10));
    }

    @Test
    void rejectsTierTenAndInvalidTargets() {
        assertThrows(RuntimeException.class, () -> EquipmentEnhancementRules.nextTier(10));
        assertThrows(RuntimeException.class, () -> EquipmentEnhancementRules.nextTier(0));
        assertThrows(RuntimeException.class, () -> EquipmentEnhancementRules.requiredCoreCode(1));
        assertThrows(RuntimeException.class, () -> EquipmentEnhancementRules.requiredCoreCode(11));
    }

    @Test
    void dismantlingUsesFixedCoreQuantitiesByTierRange() {
        assertEquals(new EquipmentEnhancementRules.DismantleReward(EquipmentEnhancementRules.BASIC_CORE, 1), EquipmentEnhancementRules.dismantleReward(1));
        assertEquals(new EquipmentEnhancementRules.DismantleReward(EquipmentEnhancementRules.BASIC_CORE, 2), EquipmentEnhancementRules.dismantleReward(3));
        assertEquals(new EquipmentEnhancementRules.DismantleReward(EquipmentEnhancementRules.ADVANCED_CORE, 1), EquipmentEnhancementRules.dismantleReward(4));
        assertEquals(new EquipmentEnhancementRules.DismantleReward(EquipmentEnhancementRules.ADVANCED_CORE, 2), EquipmentEnhancementRules.dismantleReward(6));
        assertEquals(new EquipmentEnhancementRules.DismantleReward(EquipmentEnhancementRules.SUPREME_CORE, 1), EquipmentEnhancementRules.dismantleReward(7));
        assertEquals(new EquipmentEnhancementRules.DismantleReward(EquipmentEnhancementRules.SUPREME_CORE, 2), EquipmentEnhancementRules.dismantleReward(9));
        assertThrows(RuntimeException.class, () -> EquipmentEnhancementRules.dismantleReward(10));
    }
}
