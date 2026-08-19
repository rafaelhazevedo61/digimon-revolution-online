package com.dro.modules.shop.api;

import com.dro.modules.shop.api.dto.request.CreateShopProductRequest;
import com.dro.modules.shop.api.dto.request.UpdateShopProductRequest;
import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.application.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Componente da camada de controller da API do módulo de Loja.
 */
@RestController
@RequestMapping("/admin/shop-products")
@RequiredArgsConstructor
public class AdminShopProductController {

    private final CreateShopProductUseCase createShopProductUseCase;
    private final ListShopProductsUseCase listShopProductsUseCase;
    private final GetShopProductUseCase getShopProductUseCase;
    private final UpdateShopProductUseCase updateShopProductUseCase;
    private final ToggleShopProductUseCase toggleShopProductUseCase;

    @PostMapping
    public ResponseEntity<AdminShopProductResponse> create(
            @RequestBody @Valid CreateShopProductRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createShopProductUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<AdminShopProductResponse>> list(
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return ResponseEntity.ok(listShopProductsUseCase.execute(activeOnly));
    }

    @GetMapping("/{code}")
    public ResponseEntity<AdminShopProductResponse> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(getShopProductUseCase.execute(code));
    }

    @PutMapping("/{code}")
    public ResponseEntity<AdminShopProductResponse> update(
            @PathVariable String code,
            @RequestBody @Valid UpdateShopProductRequest request
    ) {
        return ResponseEntity.ok(updateShopProductUseCase.execute(code, request));
    }

    @PatchMapping("/{code}/toggle-active")
    public ResponseEntity<AdminShopProductResponse> toggleActive(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(toggleShopProductUseCase.execute(code));
    }
}
