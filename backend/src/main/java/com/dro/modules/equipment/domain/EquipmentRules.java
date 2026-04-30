package com.dro.modules.equipment.domain;

import java.util.List;

public class EquipmentRules {

    public static void validateEquip(Equipment equipment) {

        if (equipment.isEquipped()) {
            throw new RuntimeException("Equipment is already equipped on a Digimon");
        }
    }

    public static int totalBonusHp(List<Equipment> equippedItems) {
        return equippedItems.stream().mapToInt(Equipment::getBonusHp).sum();
    }

    public static int totalBonusAttack(List<Equipment> equippedItems) {
        return equippedItems.stream().mapToInt(Equipment::getBonusAttack).sum();
    }

    public static int totalBonusDefense(List<Equipment> equippedItems) {
        return equippedItems.stream().mapToInt(Equipment::getBonusDefense).sum();
    }
}
