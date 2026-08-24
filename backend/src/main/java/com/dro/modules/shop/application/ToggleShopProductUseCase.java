package com.dro.modules.shop.application;

import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Loja.
 */
@Service
public class ToggleShopProductUseCase {
    private final ShopProductRepository shopProductRepository;

    @CacheEvict(cacheNames = "shopCatalog", key = "\'active\'")
    @Transactional
    public AdminShopProductResponse execute(String code) {
        ShopProductEntity entity = shopProductRepository.findById(code).orElseThrow(() -> new NotFoundException("Shop product not found: " + code));
        entity.setActive(!entity.isActive());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy("admin");
        shopProductRepository.save(entity);
        return AdminShopProductResponse.from(entity);
    }

    public ToggleShopProductUseCase(final ShopProductRepository shopProductRepository) {
        this.shopProductRepository = shopProductRepository;
    }
}
