package com.dro.modules.shop.api;

import com.dro.modules.shop.api.dto.BuyShopProductResponse;
import com.dro.modules.shop.api.dto.request.BuyShopProductRequest;
import com.dro.modules.shop.api.dto.request.SellShopProductRequest;
import com.dro.modules.shop.api.dto.response.SellShopProductResponse;
import com.dro.modules.shop.api.dto.response.ShopCatalogResponse;
import com.dro.modules.shop.api.dto.response.ShopProductResponse;
import com.dro.modules.shop.application.BuyShopProductUseCase;
import com.dro.modules.shop.application.GetShopProductsUseCase;
import com.dro.modules.shop.application.SellShopProductUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Componente da camada de controller da API do módulo de Loja.
 */
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final GetShopProductsUseCase getShopProductsUseCase;
    private final BuyShopProductUseCase buyShopProductUseCase;
    private final SellShopProductUseCase sellShopProductUseCase;

    @GetMapping
    public ResponseEntity<ShopCatalogResponse> getProducts() {
        return ResponseEntity.ok(getShopProductsUseCase.execute());
    }

    @PostMapping("/buy")
    public ResponseEntity<BuyShopProductResponse> buy(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid BuyShopProductRequest request
    ) {
        return ResponseEntity.ok(buyShopProductUseCase.execute(authorization, request));
    }

    @PostMapping("/sell")
    public ResponseEntity<SellShopProductResponse> sell(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SellShopProductRequest request
    ) {
        return ResponseEntity.ok(sellShopProductUseCase.execute(authorization, request));
    }
}