package com.dro.modules.equipment.domain;

import com.dro.shared.exception.ConflictException;

import java.util.List;
import java.util.Map;

public class EquipmentRules {

    public static void validateEquip(Equipment equipment) {

        if (equipment.isEquipped()) {
            throw new ConflictException("Equipment is already equipped on a Digimon");
        }
    }

    public static final int MAX_REFINEMENT_LEVEL = 10;

    // Set bonus percentages: [2-piece HP%, 2-piece ATK%, 2-piece DEF%, 3-piece HP%, 3-piece ATK%, 3-piece DEF%]
    private static final Map<String, int[]> SET_BONUSES = Map.of(
            "BERSERKER",  new int[]{ 0, 10,  0,  0, 20,  0},
            "GUARDIAN",   new int[]{ 5,  0, 10, 10,  0, 20},
            "VITALITY",   new int[]{10,  0,  0, 20,  0,  0},
            "BALANCED",   new int[]{ 5,  5,  5, 10, 10, 10}
    );

    public static int totalBonusHp(List<Equipment> equippedItems) {
        int base = equippedItems.stream().mapToInt(Equipment::getEffectiveBonusHp).sum();
        return base + (int) Math.round(base * getSetBonusPercent(equippedItems, 0, 3) / 100.0);
    }

    public static int totalBonusAttack(List<Equipment> equippedItems) {
        int base = equippedItems.stream().mapToInt(Equipment::getEffectiveBonusAttack).sum();
        return base + (int) Math.round(base * getSetBonusPercent(equippedItems, 1, 4) / 100.0);
    }

    public static int totalBonusDefense(List<Equipment> equippedItems) {
        int base = equippedItems.stream().mapToInt(Equipment::getEffectiveBonusDefense).sum();
        return base + (int) Math.round(base * getSetBonusPercent(equippedItems, 2, 5) / 100.0);
    }

    private static int getSetBonusPercent(List<Equipment> equippedItems, int idx2, int idx3) {
        String dominantSet = findDominantSet(equippedItems);
        if (dominantSet == null) return 0;

        int[] bonuses = SET_BONUSES.get(dominantSet);
        if (bonuses == null) return 0;

        long count = equippedItems.stream()
                .filter(e -> dominantSet.equals(e.getSetCode()))
                .count();

        if (count >= 3) return bonuses[idx3];
        if (count >= 2) return bonuses[idx2];
        return 0;
    }

    private static String findDominantSet(List<Equipment> equippedItems) {
        return equippedItems.stream()
                .filter(e -> e.getSetCode() != null)
                .collect(java.util.stream.Collectors.groupingBy(Equipment::getSetCode, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static SetBonusInfo getSetBonusInfo(List<Equipment> equippedItems) {
        String dominantSet = findDominantSet(equippedItems);
        if (dominantSet == null) return new SetBonusInfo(null, 0, 0, 0, 0);

        int[] bonuses = SET_BONUSES.get(dominantSet);
        if (bonuses == null) return new SetBonusInfo(null, 0, 0, 0, 0);

        long count = equippedItems.stream()
                .filter(e -> dominantSet.equals(e.getSetCode()))
                .count();

        if (count >= 3) return new SetBonusInfo(dominantSet, (int) count, bonuses[3], bonuses[4], bonuses[5]);
        if (count >= 2) return new SetBonusInfo(dominantSet, (int) count, bonuses[0], bonuses[1], bonuses[2]);
        return new SetBonusInfo(null, 0, 0, 0, 0);
    }

    public record SetBonusInfo(String setCode, int pieceCount, int bonusHpPercent, int bonusAtkPercent, int bonusDefPercent) {}

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
