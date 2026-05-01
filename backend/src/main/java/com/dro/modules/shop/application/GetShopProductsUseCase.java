package com.dro.modules.shop.application;

import com.dro.modules.shop.api.dto.response.ShopProductResponse;
import com.dro.modules.shop.domain.ShopCatalog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetShopProductsUseCase {

    public List<ShopProductResponse> execute() {
        return ShopCatalog.getProducts()
                .stream()
                .map(ShopProductResponse::from)
                .toList();
    }
}