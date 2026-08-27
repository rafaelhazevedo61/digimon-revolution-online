package com.dro.modules.digimon.api.dto.response;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record RebirthPreviewResponse(
        boolean eligible,
        String reason,
        int currentRebirthCount,
        int newRebirthCount,
        int costBits,
        int costDataCore,
        int costDigitalData,
        int currentDigitalData,
        int currentBits,
        int remainingBitsAfterRebirth,
        IvRangeResponse hpIvRange,
        IvRangeResponse attackIvRange,
        IvRangeResponse defenseIvRange,
        double statMultiplier
) {
}