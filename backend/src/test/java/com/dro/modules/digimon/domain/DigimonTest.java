package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.equipment.domain.EquipmentSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DigimonTest {

    private Digimon createDigimon() {
        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(UUID.randomUUID())
                .name("Agumon")
                .type("FIRE")
                .stage(Stage.BABY)
                .level(1)
                .experience(0)
                .hp(10)
                .attack(5)
                .defense(5)
                .ivHp(50)
                .ivAttack(50)
                .ivDefense(50)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .trait(null)
                .energy(10)
                .maxEnergy(10)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(0)
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void gainExperience_levelsUp_whenEnoughXp() {
        Digimon digimon = createDigimon();
        // Level 1 needs 100 xp to level up (xpToNextLevel = level * 100)
        // COMMON rarity = 1.0x, FIGHTER personality = 1.0x xp, null trait = 1.0x
        digimon.gainExperience(100);

        assertEquals(2, digimon.getLevel());
    }

    @Test
    void gainExperience_returnsTheAmountAppliedAfterDigimonMultipliers() {
        Digimon digimon = createDigimon();
        digimon.setRarity(Rarity.EPIC);
        digimon.setPersonality(Personality.LIVELY);

        int appliedXp = digimon.gainExperience(102);

        assertEquals(123, appliedXp);
        assertEquals(2, digimon.getLevel());
        assertEquals(23, digimon.getExperience());
    }

    @Test
    void gainExperience_increasesStats_onLevelUp() {
        Digimon digimon = createDigimon();
        int initialHp = digimon.getHp();
        int initialAtk = digimon.getAttack();
        int initialDef = digimon.getDefense();

        digimon.gainExperience(100);

        assertEquals(initialHp + 2, digimon.getHp());
        assertEquals(initialAtk + 1, digimon.getAttack());
        assertEquals(initialDef + 1, digimon.getDefense());
    }

    @Test
    void gainExperience_multiplelevels_withEnoughXp() {
        Digimon digimon = createDigimon();
        // Level 1->2 = 100, Level 2->3 = 200 => total 300
        digimon.gainExperience(300);
        assertEquals(3, digimon.getLevel());
    }

    @Test
    void gainExperience_doesNotExceedMaxLevel() {
        Digimon digimon = createDigimon();
        digimon.setLevel(99);
        digimon.gainExperience(999999);
        assertEquals(100, digimon.getLevel());
    }

    @Test
    void consumeEnergy_reducesEnergy() {
        Digimon digimon = createDigimon();
        digimon.setEnergy(10);
        digimon.consumeEnergy(3);
        assertEquals(7, digimon.getEnergy());
    }

    @Test
    void consumeEnergy_throwsWhenInsufficient() {
        Digimon digimon = createDigimon();
        digimon.setEnergy(2);
        assertThrows(RuntimeException.class, () -> digimon.consumeEnergy(5));
    }

    @Test
    void regenerateEnergy_recoversAfterTime() {
        Digimon digimon = createDigimon();
        digimon.setEnergy(5);
        digimon.setMaxEnergy(10);
        // 10 minutes ago = 2 energy recovered (1 per 5 min)
        digimon.setLastEnergyUpdate(Instant.now().minusSeconds(600));

        digimon.regenerateEnergy();

        assertEquals(7, digimon.getEnergy());
    }

    @Test
    void regenerateEnergy_doesNotExceedMax() {
        Digimon digimon = createDigimon();
        digimon.setEnergy(9);
        digimon.setMaxEnergy(10);
        digimon.setLastEnergyUpdate(Instant.now().minusSeconds(6000));

        digimon.regenerateEnergy();

        assertEquals(10, digimon.getEnergy());
    }

    @Test
    void regenerateEnergy_doesNothingWhenFull() {
        Digimon digimon = createDigimon();
        digimon.setEnergy(10);
        digimon.setMaxEnergy(10);

        digimon.regenerateEnergy();

        assertEquals(10, digimon.getEnergy());
    }

    @Test
    void equipmentSlots_weaponEquipAndClear() {
        Digimon digimon = createDigimon();
        UUID weaponId = UUID.randomUUID();

        digimon.equipWeapon(weaponId);
        assertEquals(weaponId, digimon.getWeaponId());
        assertEquals(weaponId, digimon.getEquipmentIdBySlot(EquipmentSlot.WEAPON));

        digimon.unequipWeapon();
        assertNull(digimon.getWeaponId());
    }

    @Test
    void equipmentSlots_armorEquipAndClear() {
        Digimon digimon = createDigimon();
        UUID armorId = UUID.randomUUID();

        digimon.equipArmor(armorId);
        assertEquals(armorId, digimon.getArmorId());
        assertEquals(armorId, digimon.getEquipmentIdBySlot(EquipmentSlot.ARMOR));

        digimon.unequipArmor();
        assertNull(digimon.getArmorId());
    }

    @Test
    void equipmentSlots_accessoryEquipAndClear() {
        Digimon digimon = createDigimon();
        UUID accessoryId = UUID.randomUUID();

        digimon.equipAccessory(accessoryId);
        assertEquals(accessoryId, digimon.getAccessoryId());
        assertEquals(accessoryId, digimon.getEquipmentIdBySlot(EquipmentSlot.ACCESSORY));

        digimon.unequipAccessory();
        assertNull(digimon.getAccessoryId());
    }

    @Test
    void setEquipmentBySlot_setsCorrectSlot() {
        Digimon digimon = createDigimon();
        UUID id = UUID.randomUUID();

        digimon.setEquipmentBySlot(EquipmentSlot.WEAPON, id);
        assertEquals(id, digimon.getWeaponId());

        digimon.setEquipmentBySlot(EquipmentSlot.ARMOR, id);
        assertEquals(id, digimon.getArmorId());

        digimon.setEquipmentBySlot(EquipmentSlot.ACCESSORY, id);
        assertEquals(id, digimon.getAccessoryId());
    }

    @Test
    void clearSlot_clearsCorrectSlot() {
        Digimon digimon = createDigimon();
        UUID id = UUID.randomUUID();

        digimon.setEquipmentBySlot(EquipmentSlot.WEAPON, id);
        digimon.clearSlot(EquipmentSlot.WEAPON);
        assertNull(digimon.getWeaponId());
    }
}
