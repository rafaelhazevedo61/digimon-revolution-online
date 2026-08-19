package com.dro.modules.arena.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Arena.
 */
public record ArenaShopResponse(
        int arenaCoins,
        List<ArenaShopProductResponse> products
) {}
