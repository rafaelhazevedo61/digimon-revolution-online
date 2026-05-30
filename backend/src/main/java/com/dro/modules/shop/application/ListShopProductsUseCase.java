package com.dro.modules.shop.application;

import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.infra.ShopProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListShopProductsUseCase {

    private final ShopProductRepository shopProductRepository;

    public List<AdminShopProductResponse> execute(Boolean activeOnly) {

        if (Boolean.TRUE.equals(activeOnly)) {
            return shopProductRepository.findByActiveTrueOrderByNameAsc()
                    .stream()
                    .map(AdminShopProductResponse::from)
                    .toList();
        }

        return shopProductRepository.findAllByOrderByNameAsc()
                .stream()
                .map(AdminShopProductResponse::from)
                .toList();
    }
}
