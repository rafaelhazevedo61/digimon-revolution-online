package com.dro.modules.mission.api.response;

import com.dro.modules.inventory.domain.ItemType;

public record RewardResponse(
        ItemType item,
        int quantity
) {}
