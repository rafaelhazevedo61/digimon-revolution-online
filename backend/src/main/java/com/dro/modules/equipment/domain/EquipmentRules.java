package com.dro.modules.equipment.domain;

import com.dro.shared.exception.ConflictException;

import java.util.List;

public class EquipmentRules {

    public static void validateEquip(Equipment equipment) {

        if (equipment.isEquipped()) {
            throw new ConflictException("Equipment is already equipped on a Digimon");
        }
    }

    public static final int MAX_REFINEMENT_LEVEL = 10;

    public static int totalBonusHp(List<Equipment> equippedItems) {
        return equippedItems.stream().mapToInt(Equipment::getEffectiveBonusHp).sum();
    }

    public static int totalBonusAttack(List<Equipment> equippedItems) {
        return equippedItems.stream().mapToInt(Equipment::getEffectiveBonusAttack).sum();
    }

    public static int totalBonusDefense(List<Equipment> equippedItems) {
        return equippedItems.stream().mapToInt(Equipment::getEffectiveBonusDefense).sum();
    }

    public static int refinementCostBits(int currentLevel) {
        return 1000 + (currentLevel * 500);
    }
}
