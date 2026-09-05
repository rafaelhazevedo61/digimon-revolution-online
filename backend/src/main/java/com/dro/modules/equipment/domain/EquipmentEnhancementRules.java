package com.dro.modules.equipment.domain;

import com.dro.shared.exception.BadRequestException;

/** Regras determinísticas da progressão T1-T10 e da desmontagem. */
public final class EquipmentEnhancementRules {
    public static final int MIN_TIER = 1;
    public static final int MAX_TIER = 10;
    public static final int REQUIRED_COPIES = 3;
    public static final String BASIC_CORE = "BASIC_ENHANCEMENT_CORE";
    public static final String ADVANCED_CORE = "ADVANCED_ENHANCEMENT_CORE";
    public static final String SUPREME_CORE = "SUPREME_ENHANCEMENT_CORE";

    private EquipmentEnhancementRules() { }

    public static int nextTier(int currentTier) {
        if (currentTier < MIN_TIER || currentTier >= MAX_TIER) {
            throw new BadRequestException("Equipment is already at the maximum tier or has an invalid tier");
        }
        return currentTier + 1;
    }

    /** Quantidade total de equipamentos, incluindo o equipamento principal, para atingir o tier. */
    public static int requiredCopiesForTargetTier(int targetTier) {
        if (targetTier < 2 || targetTier > MAX_TIER) {
            throw new BadRequestException("Invalid enhancement target tier");
        }
        if (targetTier <= 5) return 3;
        if (targetTier <= 8) return 4;
        return 5;
    }

    public static String requiredCoreCode(int targetTier) {
        if (targetTier < 2 || targetTier > MAX_TIER) {
            throw new BadRequestException("Invalid enhancement target tier");
        }
        if (targetTier <= 4) return BASIC_CORE;
        if (targetTier <= 7) return ADVANCED_CORE;
        return SUPREME_CORE;
    }

    /** Retorna o núcleo e a quantidade fixa gerados pela desmontagem. */
    public static DismantleReward dismantleReward(int equipmentTier) {
        if (equipmentTier < MIN_TIER || equipmentTier >= MAX_TIER) {
            throw new BadRequestException("This equipment tier cannot be dismantled");
        }
        if (equipmentTier <= 3) return new DismantleReward(BASIC_CORE, equipmentTier == 3 ? 2 : 1);
        if (equipmentTier <= 6) return new DismantleReward(ADVANCED_CORE, equipmentTier == 6 ? 2 : 1);
        return new DismantleReward(SUPREME_CORE, equipmentTier == 9 ? 2 : 1);
    }

    public record DismantleReward(String coreCode, int quantity) { }
}
