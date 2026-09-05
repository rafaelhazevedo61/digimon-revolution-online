package com.dro.modules.equipment.api.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resultado agregado da desmontagem de vários equipamentos. */
public record DismantleEquipmentBatchResponse(
        int dismantledCount,
        Map<String, Integer> coresGranted,
        List<UUID> dismantledEquipmentIds
) {
}
