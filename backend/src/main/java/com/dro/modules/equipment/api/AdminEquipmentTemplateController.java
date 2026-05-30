package com.dro.modules.equipment.api;

import com.dro.modules.equipment.api.dto.request.CreateEquipmentTemplateRequest;
import com.dro.modules.equipment.api.dto.request.UpdateEquipmentTemplateRequest;
import com.dro.modules.equipment.api.dto.response.EquipmentTemplateResponse;
import com.dro.modules.equipment.application.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/equipment-templates")
@RequiredArgsConstructor
public class AdminEquipmentTemplateController {

    private final CreateEquipmentTemplateUseCase createEquipmentTemplateUseCase;
    private final ListEquipmentTemplatesUseCase listEquipmentTemplatesUseCase;
    private final GetEquipmentTemplateUseCase getEquipmentTemplateUseCase;
    private final UpdateEquipmentTemplateUseCase updateEquipmentTemplateUseCase;
    private final ToggleEquipmentTemplateUseCase toggleEquipmentTemplateUseCase;

    @PostMapping
    public ResponseEntity<EquipmentTemplateResponse> create(
            @RequestBody @Valid CreateEquipmentTemplateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createEquipmentTemplateUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<EquipmentTemplateResponse>> list(
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return ResponseEntity.ok(listEquipmentTemplatesUseCase.execute(activeOnly));
    }

    @GetMapping("/{name}")
    public ResponseEntity<EquipmentTemplateResponse> getByName(
            @PathVariable String name
    ) {
        return ResponseEntity.ok(getEquipmentTemplateUseCase.execute(name));
    }

    @PutMapping("/{name}")
    public ResponseEntity<EquipmentTemplateResponse> update(
            @PathVariable String name,
            @RequestBody @Valid UpdateEquipmentTemplateRequest request
    ) {
        return ResponseEntity.ok(updateEquipmentTemplateUseCase.execute(name, request));
    }

    @PatchMapping("/{name}/toggle-active")
    public ResponseEntity<EquipmentTemplateResponse> toggleActive(
            @PathVariable String name
    ) {
        return ResponseEntity.ok(toggleEquipmentTemplateUseCase.execute(name));
    }
}
