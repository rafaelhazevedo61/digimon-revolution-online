package com.dro.modules.clan.api.dto.response;

import com.dro.modules.clan.domain.ClanRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanMemberResponse(
        UUID id,
        String username,
        ClanRole role,
        LocalDateTime joinedAt,
        Integer activeDigimonPower
) {
}
