package com.dro.modules.digimon.api.dto.response;

public record RebirthPreviewResponse(
        boolean eligible,
        String reason,
        int currentRebirthCount,
        int newRebirthCount,
        int costBits,
        int costDataCore,
        int currentBits,
        int remainingBitsAfterRebirth,
        IvRangeResponse hpIvRange,
        IvRangeResponse attackIvRange,
        IvRangeResponse defenseIvRange,
        double statMultiplier
) {
}