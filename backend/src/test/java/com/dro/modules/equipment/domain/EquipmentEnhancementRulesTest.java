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
}
