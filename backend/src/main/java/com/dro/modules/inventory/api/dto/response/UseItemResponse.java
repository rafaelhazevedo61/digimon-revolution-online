package com.dro.modules.inventory.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

public record UseItemResponse(
        ItemType itemType,
        int quantity,
        int xpGranted,
        int previousLevel,
        int currentLevel,
        boolean levelUp,
        String message,
        com.dro.modules.mission.api.dto.response.NewlyUnlockedContentResponse newlyUnlockedContent
) {
}
