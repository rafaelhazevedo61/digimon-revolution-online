package com.dro.modules.inventory.application;

import com.dro.modules.inventory.api.dto.response.ItemDefinitionPageResponse;
import com.dro.modules.inventory.api.dto.response.ItemDefinitionResponse;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Inventário.
 */
@Service
@RequiredArgsConstructor
public class GetItemDefinitionsUseCase {

    private final ItemDefinitionRepository itemDefinitionRepository;

    public ItemDefinitionPageResponse execute(
            String category,
            String rarity,
            Boolean usable,
            Boolean sellable,
            Boolean tradable,
            Pageable pageable
    ) {
        Page<ItemDefinitionResponse> items = itemDefinitionRepository.findCatalog(
                        normalize(category),
                        normalize(rarity),
                        usable,
                        sellable,
                        tradable,
                        pageable
                )
                .map(ItemDefinitionResponse::from);

        return ItemDefinitionPageResponse.from(items);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toUpperCase();
    }
}