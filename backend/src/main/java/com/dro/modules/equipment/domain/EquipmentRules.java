package com.dro.modules.equipment.domain;

import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.modules.digimon.domain.Digimon;

import java.util.List;
import java.util.Map;

/**
 * Regras de equipamento, bônus de set e refinamento.
 *
 * <p>Um set ativa bônus com duas peças e bônus maiores com três peças. O
 * refinamento vai de {@code +0} a {@code +10}; o custo aumenta conforme o nível
 * atual e a taxa de sucesso diminui nas etapas superiores.</p>
 */
public class EquipmentRules {

    /** Impede equipar novamente uma peça que já está vinculada a um Digimon. */
    public static void validateEquip(Equipment equipment) {

        if (equipment.isEquipped()) {
            throw new ConflictException("Equipment is already equipped on a Digimon");
        }
    }

    public static void validateAscensionEquipRequirement(Equipment equipment, Digimon digimon) {
        if (equipment.getAscensionLevel() <= 0) return;
        int required = ascensionRebirthRequirement(equipment.getAscensionLevel());
        if (digimon.getRebirthCount() < required) {
            throw new UnprocessableException("Este equipamento exige um Digimon com pelo menos " + required + " Renascimento(s)");
        }
    }

    /** Maior nível de refinamento permitido. */
    public static final int MAX_REFINEMENT_LEVEL = 11;
    public static final int REFINEMENT_SUCCESS_BOOST_POINTS = 10;
    public static final int MAX_REFINEMENT_BREAK_LEVEL = 10;
    public static final int REFINEMENT_BREAK_CHANCE_AT_MAX = 25;
    public static final int MAX_ASCENSION_LEVEL = 3;
    public static final int ASCENSION_REFINEMENT_REQUIREMENT = MAX_REFINEMENT_LEVEL;
    private static final int[] ASCENSION_REBIRTH_REQUIREMENTS = {0, 1, 10, 20};
    private static final int[] ASCENSION_CORE_COSTS = {0, 10, 30, 100};
    private static final int[] ASCENSION_BITS_COSTS = {0, 100_000, 500_000, 2_000_000};
    private static final double[] ASCENSION_MULTIPLIERS = {1.0, 1.05, 1.12, 1.20};

    public static int ascensionRebirthRequirement(int targetLevel) {
        validateAscensionTarget(targetLevel);
        return ASCENSION_REBIRTH_REQUIREMENTS[targetLevel];
    }

    public static int ascensionCoreCost(int targetLevel) {
        validateAscensionTarget(targetLevel);
        return ASCENSION_CORE_COSTS[targetLevel];
    }

    public static int ascensionBitsCost(int targetLevel) {
        validateAscensionTarget(targetLevel);
        return ASCENSION_BITS_COSTS[targetLevel];
    }

    public static double ascensionMultiplier(int level) {
        if (level < 0 || level > MAX_ASCENSION_LEVEL) return 1.0;
        return ASCENSION_MULTIPLIERS[level];
    }

    public static void validateAscensionTarget(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_ASCENSION_LEVEL) {
            throw new IllegalArgumentException("Invalid ascension target: " + targetLevel);
        }
    }

    // Set bonus percentages: [2-piece HP%, 2-piece ATK%, 2-piece DEF%, 3-piece HP%, 3-piece ATK%, 3-piece DEF%]
    private static final Map<String, int[]> SET_BONUSES = Map.of(
            "BERSERKER",  new int[]{ 0, 10,  0,  0, 20,  0},
            "GUARDIAN",   new int[]{ 5,  0, 10, 10,  0, 20},
            "VITALITY",   new int[]{10,  0,  0, 20,  0,  0},
            "BALANCED",   new int[]{ 5,  5,  5, 10, 10, 10}
    );

    /** Calcula o bônus total de HP das peças e do set dominante. */
    public static int totalBonusHp(List<Equipment> equippedItems) {
        int base = equippedItems.stream().mapToInt(Equipment::getEffectiveBonusHp).sum();
        return base + (int) Math.round(base * getSetBonusPercent(equippedItems, 0, 3) / 100.0);
    }

    /** Calcula o bônus total de ATK das peças e do set dominante. */
    public static int totalBonusAttack(List<Equipment> equippedItems) {
        int base = equippedItems.stream().mapToInt(Equipment::getEffectiveBonusAttack).sum();
        return base + (int) Math.round(base * getSetBonusPercent(equippedItems, 1, 4) / 100.0);
    }

    /** Calcula o bônus total de DEF das peças e do set dominante. */
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

    /** Retorna o set dominante, quantidade de peças e percentuais ativos. */
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

    /** Informações calculadas sobre o bônus de set atualmente ativo. */
    public record SetBonusInfo(
            String setCode,
            int pieceCount,
            int bonusHpPercent,
            int bonusAtkPercent,
            int bonusDefPercent
    ) {
    }

    /**
     * Calcula o custo de Bits para tentar o próximo nível de refinamento.
     *
     * @param currentLevel nível atual da peça
     * @return custo em Bits
     */
    public static int refinementCostBits(int currentLevel) {
        return 1000 + (currentLevel * 500);
    }

    private static final int[] REFINEMENT_SUCCESS_RATE = {
        100, // +0 → +1
         90, // +1 → +2
         80, // +2 → +3
         70, // +3 → +4
         60, // +4 → +5
         50, // +5 → +6
         40, // +6 → +7
         30, // +7 → +8
         20, // +8 → +9
        10, // +9 → +10
        5   // +10 → +11
    };

    /**
     * Retorna a chance de sucesso da tentativa para o próximo nível.
     *
     * @param currentLevel nível atual da peça
     * @return percentual entre 0 e 100; zero fora da faixa válida
     */
    public static int refinementSuccessRate(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= REFINEMENT_SUCCESS_RATE.length) {
            return 0;
        }
        return REFINEMENT_SUCCESS_RATE[currentLevel];
    }

    /** Retorna a chance de quebra ao tentar o refinamento +10 para +11. */
    public static int refinementBreakChance(int currentLevel) {
        return currentLevel == MAX_REFINEMENT_BREAK_LEVEL ? REFINEMENT_BREAK_CHANCE_AT_MAX : 0;
    }

    /** Aplica pontos percentuais de bônus sem ultrapassar 100%. */
    public static int refinementSuccessRate(int currentLevel, int bonusPoints) {
        return Math.min(100, refinementSuccessRate(currentLevel) + Math.max(0, bonusPoints));
    }
}
