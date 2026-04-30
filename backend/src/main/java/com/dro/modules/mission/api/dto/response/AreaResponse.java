package com.dro.modules.mission.api.dto.response;

import com.dro.modules.mission.domain.Area;

public record AreaResponse(
        Area area,
        boolean unlocked
) {}