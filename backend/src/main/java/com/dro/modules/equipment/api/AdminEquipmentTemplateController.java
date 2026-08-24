package com.dro.modules.equipment.api;

import com.dro.modules.equipment.api.dto.request.CreateEquipmentTemplateRequest;
import com.dro.modules.equipment.api.dto.request.GrantEquipmentRequest;
import com.dro.modules.equipment.api.dto.request.UpdateEquipmentTemplateRequest;
import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.api.dto.response.GrantEquipmentResponse;
import com.dro.modules.equipment.application.*;
import com.dro.shared.audit.AdminAuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Equipamentos.
 */
@RestController
@RequestMapping("/admin/equipment-templates")
public class AdminEquipmentTemplateController {
    private final CreateEquipmentTemplateUseCase createEquipmentTemplateUseCase;
    private final ListEquipmentTemplatesUseCase listEquipmentTemplatesUseCase;
    private final GetEquipmentTemplateUseCase getEquipmentTemplateUseCase;
    private final UpdateEquipmentTemplateUseCase updateEquipmentTemplateUseCase;
    private final ToggleEquipmentTemplateUseCase toggleEquipmentTemplateUseCase;
    private final GrantEquipmentUseCase grantEquipmentUseCase;
    private final AdminAuditService adminAuditService;

    @PostMapping
    public ResponseEntity<EquipmentTemplateResponse> create(@RequestBody @Valid CreateEquipmentTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createEquipmentTemplateUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<EquipmentTemplateResponse>> list(@RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(listEquipmentTemplatesUseCase.execute(activeOnly));
    }

    @GetMapping("/{name}")
    public ResponseEntity<EquipmentTemplateResponse> getByName(@PathVariable String name) {
        return ResponseEntity.ok(getEquipmentTemplateUseCase.execute(name));
    }

    @PutMapping("/{name}")
    public ResponseEntity<EquipmentTemplateResponse> update(@PathVariable String name, @RequestBody @Valid UpdateEquipmentTemplateRequest request) {
        return ResponseEntity.ok(updateEquipmentTemplateUseCase.execute(name, request));
    }

    @PatchMapping("/{name}/toggle-active")
    public ResponseEntity<EquipmentTemplateResponse> toggleActive(@PathVariable String name) {
        return ResponseEntity.ok(toggleEquipmentTemplateUseCase.execute(name));
    }

    @PostMapping("/grant")
    public ResponseEntity<GrantEquipmentResponse> grant(@RequestHeader("Authorization") String authorization, @RequestBody @Valid GrantEquipmentRequest request) {
        UUID equipmentId = grantEquipmentUseCase.execute(request.digimonId(), request.templateName(), request.rarity());
        adminAuditService.success(authorization, "ADMIN_EQUIPMENT_GRANT", "Equipment", equipmentId.toString(), "grant", "Equipamento concedido ao Digimon", Map.of("targetDigimonId", request.digimonId().toString(), "templateName", request.templateName(), "rarity", request.rarity() == null ? "ROLLED" : request.rarity().name()));
        return ResponseEntity.ok(new GrantEquipmentResponse(equipmentId, "Equipment granted successfully"));
    }

    public AdminEquipmentTemplateController(final CreateEquipmentTemplateUseCase createEquipmentTemplateUseCase, final ListEquipmentTemplatesUseCase listEquipmentTemplatesUseCase, final GetEquipmentTemplateUseCase getEquipmentTemplateUseCase, final UpdateEquipmentTemplateUseCase updateEquipmentTemplateUseCase, final ToggleEquipmentTemplateUseCase toggleEquipmentTemplateUseCase, final GrantEquipmentUseCase grantEquipmentUseCase, final AdminAuditService adminAuditService) {
        this.createEquipmentTemplateUseCase = createEquipmentTemplateUseCase;
        this.listEquipmentTemplatesUseCase = listEquipmentTemplatesUseCase;
        this.getEquipmentTemplateUseCase = getEquipmentTemplateUseCase;
        this.updateEquipmentTemplateUseCase = updateEquipmentTemplateUseCase;
        this.toggleEquipmentTemplateUseCase = toggleEquipmentTemplateUseCase;
        this.grantEquipmentUseCase = grantEquipmentUseCase;
        this.adminAuditService = adminAuditService;
    }
}
