package com.dro.modules.equipment.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentTest {

    private Equipment createEquipment() {
        return Equipment.builder()
                .id(UUID.randomUUID())
                .digimonId(UUID.randomUUID())
                .name("Iron Claw")
                .slot(EquipmentSlot.WEAPON)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(0)
                .bonusAttack(5)
                .bonusDefense(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void equip_setsEquippedToTrue() {
        Equipment equipment = createEquipment();
        assertFalse(equipment.isEquipped());

        equipment.equip();
        assertTrue(equipment.isEquipped());
    }

    @Test
    void unequip_setsEquippedToFalse() {
        Equipment equipment = createEquipment();
        equipment.equip();
        assertTrue(equipment.isEquipped());

        equipment.unequip();
        assertFalse(equipment.isEquipped());
    }

    @Test
    void defaultEquipped_isFalse() {
        Equipment equipment = createEquipment();
        assertFalse(equipment.isEquipped());
    }
}
