package com.dro.modules.clan.api.dto.request;

import com.dro.modules.clan.domain.ClanRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull ClanRole role
) {
}
