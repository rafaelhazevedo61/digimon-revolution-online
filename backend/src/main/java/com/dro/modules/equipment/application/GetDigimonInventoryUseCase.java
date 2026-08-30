package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.UUID;

/** Lista somente os equipamentos disponíveis, ainda não equipados, do jogador. */
@Service
public class GetDigimonInventoryUseCase {
    private final EquipmentRepository equipmentRepository;

    public java.util.List<EquipmentResponse> execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        return equipmentRepository.findByPlayerIdAndEquippedFalse(playerId).stream().map(EquipmentResponse::from).toList();
    }

    /** Compatibilidade durante a migração; o parâmetro Digimon não restringe mais a posse. */
    @Deprecated
    public java.util.List<EquipmentResponse> execute(String token, UUID ignoredDigimonId) {
        return execute(token);
    }

    public GetDigimonInventoryUseCase(final EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }
}
