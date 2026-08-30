package com.dro.modules.mission.api.dto.response;

/**
 * Detalha como o valor final de uma recompensa de missão foi calculado.
 */
public record MissionRewardBreakdownResponse(
        int baseAmount,
        double missionProgressMultiplier,
        double clanMultiplier,
        int eventMultiplier,
        double digimonMultiplier,
        double combinedMultiplier,
        double effectiveMultiplier,
        int amountBeforeDigimonMultiplier,
        int finalAmount
) {
}
