package com.dro.modules.mission.api.dto.response;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.mission.domain.Area;

/**
 * Contrato de dados do módulo de Missões.
 */
public record AreaResponse(
        Area area,
        Stage requiredStage,
        boolean unlocked
) {}
