package com.dro.modules.equipment.api.dto.response;

import java.util.UUID;

/** Resultado da conversão de um equipamento em núcleo. */
public record DismantleEquipmentResponse(
        UUID dismantledEquipmentId,
        int equipmentTier,
        String coreCode,
        int quantityGranted
) {
}
