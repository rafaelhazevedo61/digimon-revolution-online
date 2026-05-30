package com.dro.modules.shop.application;

import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ToggleShopProductUseCase {

    private final ShopProductRepository shopProductRepository;

    public AdminShopProductResponse execute(String code) {

        ShopProductEntity entity = shopProductRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("Shop product not found: " + code));

        entity.setActive(!entity.isActive());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");

        shopProductRepository.save(entity);

        return AdminShopProductResponse.from(entity);
    }
}
