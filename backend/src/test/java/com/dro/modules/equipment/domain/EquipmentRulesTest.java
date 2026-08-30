package com.dro.modules.equipment.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentRulesTest {

    private Equipment createEquipment(int bonusHp, int bonusAttack, int bonusDefense, boolean equipped) {
        return Equipment.builder()
                .id(UUID.randomUUID())
                .digimonId(UUID.randomUUID())
                .name("Test Equipment")
                .slot(EquipmentSlot.WEAPON)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(bonusHp)
                .bonusAttack(bonusAttack)
                .bonusDefense(bonusDefense)
                .equipped(equipped)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void validateEquip_throwsWhenAlreadyEquipped() {
        Equipment equipment = createEquipment(0, 5, 0, true);
        assertThrows(RuntimeException.class, () -> EquipmentRules.validateEquip(equipment));
    }

    @Test
    void validateEquip_doesNotThrowWhenNotEquipped() {
        Equipment equipment = createEquipment(0, 5, 0, false);
        assertDoesNotThrow(() -> EquipmentRules.validateEquip(equipment));
    }

    @Test
    void totalBonusHp_sumsAllEquipments() {
        List<Equipment> items = List.of(
                createEquipment(10, 0, 0, true),
                createEquipment(20, 0, 0, true),
                createEquipment(5, 0, 0, true)
        );
        assertEquals(35, EquipmentRules.totalBonusHp(items));
    }

    @Test
    void totalBonusAttack_sumsAllEquipments() {
        List<Equipment> items = List.of(
                createEquipment(0, 5, 0, true),
                createEquipment(0, 12, 0, true)
        );
        assertEquals(17, EquipmentRules.totalBonusAttack(items));
    }

    @Test
    void totalBonusDefense_sumsAllEquipments() {
        List<Equipment> items = List.of(
                createEquipment(0, 0, 5, true),
                createEquipment(0, 0, 25, true)
        );
        assertEquals(30, EquipmentRules.totalBonusDefense(items));
    }

    @Test
    void totalBonus_emptyList_returnsZero() {
        List<Equipment> empty = List.of();
        assertEquals(0, EquipmentRules.totalBonusHp(empty));
        assertEquals(0, EquipmentRules.totalBonusAttack(empty));
        assertEquals(0, EquipmentRules.totalBonusDefense(empty));
    }

    @Test
    void ascension_hasExpectedProgressionAndCap() {
        assertEquals(3, EquipmentRules.MAX_ASCENSION_LEVEL);
        assertEquals(EquipmentRules.MAX_REFINEMENT_LEVEL, EquipmentRules.ASCENSION_REFINEMENT_REQUIREMENT);
        assertEquals(1, EquipmentRules.ascensionRebirthRequirement(1));
        assertEquals(10, EquipmentRules.ascensionRebirthRequirement(2));
        assertEquals(20, EquipmentRules.ascensionRebirthRequirement(3));
        assertEquals(10, EquipmentRules.ascensionCoreCost(1));
        assertEquals(30, EquipmentRules.ascensionCoreCost(2));
        assertEquals(100, EquipmentRules.ascensionCoreCost(3));
    }

    @Test
    void ascension_multiplier_isAppliedOnlyAfterFirstAscension() {
        Equipment equipment = createEquipment(100, 0, 0, false);
        equipment.setRefinementLevel(EquipmentRules.MAX_REFINEMENT_LEVEL);
        assertEquals(122, equipment.getEffectiveBonusHp());
        equipment.setAscensionLevel(3);
        assertEquals(146, equipment.getEffectiveBonusHp());
    }
}
