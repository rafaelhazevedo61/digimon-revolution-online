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

    private static final int[] REFINEMENT_SUCCESS_RATE = {
        100, // +0 → +1
         95, // +1 → +2
         90, // +2 → +3
         80, // +3 → +4
         70, // +4 → +5
         60, // +5 → +6
         50, // +6 → +7
         40, // +7 → +8
         30, // +8 → +9
         20  // +9 → +10
    };

    public static int refinementSuccessRate(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= REFINEMENT_SUCCESS_RATE.length) {
            return 0;
        }
        return REFINEMENT_SUCCESS_RATE[currentLevel];
    }
}
