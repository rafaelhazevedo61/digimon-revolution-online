package com.dro.modules.equipment.domain;

import com.dro.shared.exception.BadRequestException;

/** Regras determinísticas da progressão T1-T10 por aprimoramento. */
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

    public static String requiredCoreCode(int targetTier) {
        if (targetTier < 2 || targetTier > MAX_TIER) {
            throw new BadRequestException("Invalid enhancement target tier");
        }
        if (targetTier <= 4) return BASIC_CORE;
        if (targetTier <= 7) return ADVANCED_CORE;
        return SUPREME_CORE;
    }
}
