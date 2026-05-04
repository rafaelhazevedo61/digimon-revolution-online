package com.dro.modules.inventory.api;

import com.dro.modules.inventory.api.dto.response.ItemDefinitionPageResponse;
import com.dro.modules.inventory.application.GetItemDefinitionsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemDefinitionController {

    private final GetItemDefinitionsUseCase getItemDefinitionsUseCase;

    @GetMapping
    public ResponseEntity<ItemDefinitionPageResponse> getItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) Boolean usable,
            @RequestParam(required = false) Boolean sellable,
            @RequestParam(required = false) Boolean tradable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                normalizePageSize(size),
                Sort.by(
                        Sort.Order.asc("category"),
                        Sort.Order.asc("name")
                )
        );

        return ResponseEntity.ok(
                getItemDefinitionsUseCase.execute(
                        category,
                        rarity,
                        usable,
                        sellable,
                        tradable,
                        pageRequest
                )
        );
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }
}