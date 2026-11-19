package com.dro.modules.clan.api.dto.response;

import com.dro.modules.clan.domain.ClanRole;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanRoleResponse(
        ClanRole role
) {
}
