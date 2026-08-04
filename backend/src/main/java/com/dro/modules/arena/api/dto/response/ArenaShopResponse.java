package com.dro.modules.arena.api.dto.response;

import java.util.List;

public record ArenaShopResponse(
        int arenaCoins,
        List<ArenaShopProductResponse> products
) {}
