package com.dro.modules.inventory.api;

import com.dro.modules.inventory.api.dto.request.UpdateItemDefinitionRequest;
import com.dro.modules.inventory.api.dto.response.ItemDefinitionResponse;
import com.dro.modules.inventory.application.UpdateItemDefinitionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints administrativos do catálogo de definições de itens.
 */
@RestController
@RequestMapping("/admin/items")
public class AdminItemDefinitionController {
    private final UpdateItemDefinitionUseCase updateItemDefinitionUseCase;

    @PutMapping("/{id}")
    public ResponseEntity<ItemDefinitionResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateItemDefinitionRequest request) {
        return ResponseEntity.ok(updateItemDefinitionUseCase.execute(id, request));
    }

    public AdminItemDefinitionController(final UpdateItemDefinitionUseCase updateItemDefinitionUseCase) {
        this.updateItemDefinitionUseCase = updateItemDefinitionUseCase;
    }
}
