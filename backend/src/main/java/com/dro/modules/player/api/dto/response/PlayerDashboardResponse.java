package com.dro.modules.player.api.dto.response;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.incubation.api.dto.response.IncubationSlotsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record PlayerDashboardResponse(
        UUID id,
        String username,
        String email,
        LocalDateTime createdAt,
        String userType,
        int digitalData,
        DigimonResponse activeDigimon,
        List<EquipmentResponse> equippedItems,
        DigimonEquipmentResponse.SetBonusResponse setBonus,
        List<InventorySummaryResponse> inventory,
        List<ActiveMissionResponse> activeMissions,
        IncubationSlotsResponse incubation,
        SlotInfoResponse slotInfo
) {

    public record SlotInfoResponse(
            int activeDigimons,
            int maxDigimonSlots,
            int storedDigimons,
            int maxStorageSlots
    ) {}
}
