package com.dro.modules.player.api.dto.response;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.incubation.api.IncubationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PlayerDashboardResponse(
        UUID id,
        String username,
        String email,
        LocalDateTime createdAt,
        DigimonResponse activeDigimon,
        List<EquipmentResponse> equippedItems,
        DigimonEquipmentResponse.SetBonusResponse setBonus,
        List<InventorySummaryResponse> inventory,
        List<ActiveMissionResponse> activeMissions,
        IncubationResponse incubation
) {}
