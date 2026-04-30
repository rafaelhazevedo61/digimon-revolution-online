package com.dro.modules.equipment.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentTemplateTest {

    @Test
    void getCatalog_returns12Items() {
        assertEquals(12, EquipmentTemplate.getCatalog().size());
    }

    @Test
    void findByName_returnsCorrectTemplate() {
        EquipmentTemplate template = EquipmentTemplate.findByName("Iron Claw");
        assertEquals("Iron Claw", template.getName());
        assertEquals(EquipmentSlot.WEAPON, template.getSlot());
        assertEquals(EquipmentRarity.COMMON, template.getRarity());
        assertEquals(0, template.getBonusHp());
        assertEquals(5, template.getBonusAttack());
        assertEquals(0, template.getBonusDefense());
    }

    @Test
    void findByName_caseInsensitive() {
        assertDoesNotThrow(() -> EquipmentTemplate.findByName("iron claw"));
        assertDoesNotThrow(() -> EquipmentTemplate.findByName("IRON CLAW"));
    }

    @Test
    void findByName_throwsWhenNotFound() {
        assertThrows(RuntimeException.class, () -> EquipmentTemplate.findByName("Nonexistent"));
    }

    @Test
    void catalog_hasAllSlotTypes() {
        var catalog = EquipmentTemplate.getCatalog();
        assertTrue(catalog.stream().anyMatch(t -> t.getSlot() == EquipmentSlot.WEAPON));
        assertTrue(catalog.stream().anyMatch(t -> t.getSlot() == EquipmentSlot.ARMOR));
        assertTrue(catalog.stream().anyMatch(t -> t.getSlot() == EquipmentSlot.ACCESSORY));
    }

    @Test
    void catalog_hasAllRarities() {
        var catalog = EquipmentTemplate.getCatalog();
        assertTrue(catalog.stream().anyMatch(t -> t.getRarity() == EquipmentRarity.COMMON));
        assertTrue(catalog.stream().anyMatch(t -> t.getRarity() == EquipmentRarity.RARE));
        assertTrue(catalog.stream().anyMatch(t -> t.getRarity() == EquipmentRarity.EPIC));
        assertTrue(catalog.stream().anyMatch(t -> t.getRarity() == EquipmentRarity.LEGENDARY));
    }
}
