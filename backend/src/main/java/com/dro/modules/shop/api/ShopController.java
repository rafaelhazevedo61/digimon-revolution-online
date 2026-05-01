package com.dro.modules.shop.api;

import com.dro.modules.shop.api.dto.BuyShopProductResponse;
import com.dro.modules.shop.api.dto.request.BuyShopProductRequest;
import com.dro.modules.shop.api.dto.response.ShopProductResponse;
import com.dro.modules.shop.application.BuyShopProductUseCase;
import com.dro.modules.shop.application.GetShopProductsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final GetShopProductsUseCase getShopProductsUseCase;
    private final BuyShopProductUseCase buyShopProductUseCase;

    @GetMapping
    public ResponseEntity<List<ShopProductResponse>> getProducts() {
        return ResponseEntity.ok(getShopProductsUseCase.execute());
    }

    @PostMapping("/buy")
    public ResponseEntity<BuyShopProductResponse> buy(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid BuyShopProductRequest request
    ) {
        return ResponseEntity.ok(buyShopProductUseCase.execute(authorization, request));
    }
}