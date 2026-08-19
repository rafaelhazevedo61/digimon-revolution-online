package com.dro.modules.shop.application;

import com.dro.modules.shop.api.dto.response.AdminShopProductResponse;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Loja.
 */
@Service
@RequiredArgsConstructor
public class GetShopProductUseCase {

    private final ShopProductRepository shopProductRepository;

    public AdminShopProductResponse execute(String code) {
        return shopProductRepository.findById(code)
                .map(AdminShopProductResponse::from)
                .orElseThrow(() -> new NotFoundException("Shop product not found: " + code));
    }
}
