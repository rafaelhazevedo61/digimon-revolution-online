package com.dro.modules.equipment.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Equipamentos não equipados selecionados para desmontagem em lote. */
public record DismantleEquipmentBatchRequest(
        @NotEmpty @Size(max = 50) List<UUID> equipmentIds
) {
}
