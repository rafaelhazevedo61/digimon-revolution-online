package com.dro.modules.shop.application;

import com.dro.modules.shop.api.dto.response.ShopCatalogResponse;
import com.dro.modules.shop.api.dto.response.ShopProductResponse;
import com.dro.modules.shop.domain.ShopProductMapper;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import com.dro.modules.shop.infra.ShopProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Loja.
 */
@Service
@RequiredArgsConstructor
public class GetShopProductsUseCase {

    private final ShopProductRepository shopProductRepository;

    public ShopCatalogResponse execute() {
        List<ShopProductResponse> products = shopProductRepository.findByActiveTrue()
                .stream()
                .map(ShopProductMapper::toProduct)
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