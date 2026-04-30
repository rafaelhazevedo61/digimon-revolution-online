package com.dro.modules.equipment.domain;

import java.util.List;
import java.util.Optional;

public class EquipmentRules {

    public static void validateEquip(Equipment equipment) {

        if (equipment.isEquipped()) {
            throw new RuntimeException("Equipment is already equipped on a Digimon");
        }
    }

    public static Optional<Equipment> findCurrentInSlot(List<Equipment> equippedItems, EquipmentSlot slot) {
        return equippedItems.stream()
                .filter(e -> e.getSlot() == slot)
                .findFirst();
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
