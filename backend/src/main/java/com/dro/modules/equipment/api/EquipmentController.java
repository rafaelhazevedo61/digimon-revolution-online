package com.dro.modules.equipment.api;

import com.dro.modules.equipment.api.dto.request.EquipRequest;
import com.dro.modules.equipment.api.dto.request.GrantEquipmentRequest;
import com.dro.modules.equipment.api.dto.request.UnequipRequest;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.api.dto.response.GrantEquipmentResponse;
import com.dro.modules.equipment.application.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final GetPlayerEquipmentUseCase getPlayerEquipmentUseCase;
    private final GetDigimonEquipmentUseCase getDigimonEquipmentUseCase;
    private final EquipUseCase equipUseCase;
    private final UnequipUseCase unequipUseCase;
    private final GrantEquipmentUseCase grantEquipmentUseCase;

    @GetMapping("/player")
    public ResponseEntity<List<EquipmentResponse>> getPlayerEquipment(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(getPlayerEquipmentUseCase.execute(authorization));
    }

    @GetMapping("/digimon/{digimonId}")
    public ResponseEntity<DigimonEquipmentResponse> getDigimonEquipment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID digimonId
    ) {
        return ResponseEntity.ok(getDigimonEquipmentUseCase.execute(authorization, digimonId));
    }

    @PostMapping("/equip")
    public ResponseEntity<String> equip(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid EquipRequest request
    ) {
        equipUseCase.execute(authorization, request.equipmentId(), request.digimonId());
        return ResponseEntity.ok("Equipment equipped successfully");
    }

    @PostMapping("/unequip")
    public ResponseEntity<String> unequip(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid UnequipRequest request
    ) {
        unequipUseCase.execute(authorization, request.equipmentId());
        return ResponseEntity.ok("Equipment unequipped successfully");
    }

    @PostMapping("/grant")
    public ResponseEntity<GrantEquipmentResponse> grant(
            @RequestBody @Valid GrantEquipmentRequest request
    ) {
        UUID equipmentId = grantEquipmentUseCase.execute(
                request.playerId(), request.templateName()
        );
        return ResponseEntity.ok(new GrantEquipmentResponse(
                equipmentId, "Equipment granted successfully"
        ));
    }
}
