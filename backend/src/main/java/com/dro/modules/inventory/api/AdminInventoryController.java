package com.dro.modules.inventory.api;

import com.dro.modules.inventory.api.dto.request.GrantItemRequest;
import com.dro.modules.inventory.api.dto.response.GrantItemResponse;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Componente da camada de controller da API do módulo de Inventário.
 */
@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final AddItemUseCase addItemUseCase;
    private final ItemDefinitionRepository itemDefinitionRepository;

    @PostMapping("/grant")
    public ResponseEntity<GrantItemResponse> grantItem(
            @RequestBody @Valid GrantItemRequest request
    ) {
        ItemDefinition itemDef = itemDefinitionRepository.findByCode(request.itemCode())
                .orElseThrow(() -> new NotFoundException("Item not found: " + request.itemCode()));

        addItemUseCase.addMaterial(request.digimonId(), itemDef, request.quantity());

        return ResponseEntity.ok(new GrantItemResponse(
                request.digimonId(),
                itemDef.getCode(),
                request.quantity(),
                "Item granted successfully"
        ));
    }

    @GetMapping("/item-definitions")
    public ResponseEntity<List<Map<String, String>>> listItemDefinitions() {
        List<ItemDefinition> items = itemDefinitionRepository.findAll();
        List<Map<String, String>> result = items.stream()
                .map(i -> Map.of(
                        "code", i.getCode(),
                        "name", i.getName(),
                        "category", i.getCategory()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
