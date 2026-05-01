package com.dro.modules.equipment.domain;

import com.dro.shared.exception.NotFoundException;

import java.util.List;

public class EquipmentTemplate {

    private final String name;
    private final EquipmentSlot slot;
    private final EquipmentRarity rarity;
    private final int bonusHp;
    private final int bonusAttack;
    private final int bonusDefense;

    public EquipmentTemplate(String name, EquipmentSlot slot, EquipmentRarity rarity,
                             int bonusHp, int bonusAttack, int bonusDefense) {
        this.name = name;
        this.slot = slot;
        this.rarity = rarity;
        this.bonusHp = bonusHp;
        this.bonusAttack = bonusAttack;
        this.bonusDefense = bonusDefense;
    }

    public String getName() { return name; }
    public EquipmentSlot getSlot() { return slot; }
    public EquipmentRarity getRarity() { return rarity; }
    public int getBonusHp() { return bonusHp; }
    public int getBonusAttack() { return bonusAttack; }
    public int getBonusDefense() { return bonusDefense; }

    private static final List<EquipmentTemplate> CATALOG = List.of(
            // Weapons
            new EquipmentTemplate("Iron Claw", EquipmentSlot.WEAPON, EquipmentRarity.COMMON, 0, 5, 0),
            new EquipmentTemplate("Steel Blade", EquipmentSlot.WEAPON, EquipmentRarity.RARE, 0, 12, 0),
            new EquipmentTemplate("Chrome Digizoid Sword", EquipmentSlot.WEAPON, EquipmentRarity.EPIC, 0, 25, 3),
            new EquipmentTemplate("Omega Blade", EquipmentSlot.WEAPON, EquipmentRarity.LEGENDARY, 5, 40, 5),

            // Armors
            new EquipmentTemplate("Leather Armor", EquipmentSlot.ARMOR, EquipmentRarity.COMMON, 5, 0, 5),
            new EquipmentTemplate("Digi-Armor", EquipmentSlot.ARMOR, EquipmentRarity.RARE, 10, 0, 12),
            new EquipmentTemplate("Chrome Digizoid Armor", EquipmentSlot.ARMOR, EquipmentRarity.EPIC, 20, 0, 25),
            new EquipmentTemplate("Royal Knight Armor", EquipmentSlot.ARMOR, EquipmentRarity.LEGENDARY, 35, 5, 40),

            // Accessories
            new EquipmentTemplate("Holy Ring", EquipmentSlot.ACCESSORY, EquipmentRarity.COMMON, 3, 3, 3),
            new EquipmentTemplate("Digivice", EquipmentSlot.ACCESSORY, EquipmentRarity.RARE, 5, 5, 5),
            new EquipmentTemplate("Crest of Courage", EquipmentSlot.ACCESSORY, EquipmentRarity.EPIC, 10, 10, 10),
            new EquipmentTemplate("Digi-Egg of Miracles", EquipmentSlot.ACCESSORY, EquipmentRarity.LEGENDARY, 20, 20, 20)
    );

    public static List<EquipmentTemplate> getCatalog() {
        return CATALOG;
    }

    public static EquipmentTemplate findByName(String name) {
        return CATALOG.stream()
                .filter(t -> t.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Equipment template not found: " + name));
    }
}
