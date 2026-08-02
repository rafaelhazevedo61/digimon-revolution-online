package com.dro.modules.arena.api.dto.response;

import java.util.List;

public record ArenaLobbyResponse(
        String digimonName,
        int rating,
        int wins,
        int losses,
        int power,
        int energy,
        int energyCost,
        List<ArenaOpponentResponse> opponents
) {}
