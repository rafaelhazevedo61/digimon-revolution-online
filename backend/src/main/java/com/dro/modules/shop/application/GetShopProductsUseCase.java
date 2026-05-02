package com.dro.modules.shop.application;

import com.dro.modules.shop.api.dto.response.ShopCatalogResponse;
import com.dro.modules.shop.api.dto.response.ShopProductResponse;
import com.dro.modules.shop.domain.ShopCatalog;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetShopProductsUseCase {

    public ShopCatalogResponse execute() {
        List<ShopProductResponse> products = ShopCatalog.getProducts()
                .stream()
                .map(ShopProductResponse::from)
                .toList();

        return new ShopCatalogResponse(
                filterByCategory(products, ShopProductCategory.POTION),
                filterByCategory(products, ShopProductCategory.MATERIAL),
                filterByCategory(products, ShopProductCategory.FRAGMENT),
                filterByCategory(products, ShopProductCategory.CONSUMABLE),
                filterByCategory(products, ShopProductCategory.EQUIPMENT)
        );
    }

    private List<ShopProductResponse> filterByCategory(
            List<ShopProductResponse> products,
            ShopProductCategory category
    ) {
        return products.stream()
                .filter(product -> product.category() == category)
                .toList();
    }
}