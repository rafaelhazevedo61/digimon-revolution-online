package com.dro.modules.inventory.api.dto.response;

import java.util.List;
import java.util.UUID;

public record TeamEnergyResponse(String teamName, List<Member> members, int energyCells, int vipCharges) {
    public record Member(UUID id, String name, int energy, int maxEnergy, int cellRecovery, int vipRecovery) {}
}
