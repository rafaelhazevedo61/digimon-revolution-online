package com.dro.modules.inventory.application;

import com.dro.modules.inventory.api.dto.request.UpdateItemDefinitionRequest;
import com.dro.modules.inventory.api.dto.response.ItemDefinitionResponse;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

/**
 * Atualiza atributos administrativos do catálogo de itens.
 *
 * <p>O código e o identificador do item permanecem imutáveis porque são
 * referenciados por inventário, evolução, loot tables, baús e loja.</p>
 */
@Service
@RequiredArgsConstructor
public class UpdateItemDefinitionUseCase {

    private static final Set<String> OFFICIAL_RARITIES = Set.of(
            "COMMON", "RARE", "EPIC", "LEGENDARY"
    );

    private final ItemDefinitionRepository itemDefinitionRepository;

    @CacheEvict(cacheNames = "itemDefinitions", allEntries = true)
    @Transactional
    public ItemDefinitionResponse execute(Long id, UpdateItemDefinitionRequest request) {
        ItemDefinition item = itemDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Definição de item não encontrada: " + id));

        String name = normalizeNameRequired(request.name(), "nome");
        String category = normalizeCodeRequired(request.category(), "categoria");
        String rarity = normalizeCodeRequired(request.rarity(), "raridade");
        if (!OFFICIAL_RARITIES.contains(rarity)) {
            throw new BadRequestException("Raridade de item não suportada: " + rarity);
        }

        if (Boolean.TRUE.equals(request.stackable())
                && (request.maxStack() == null || request.maxStack() < 1)) {
            throw new BadRequestException("O acúmulo máximo é obrigatório para itens acumuláveis");
        }

        item.setName(name);
        item.setDescription(normalizeOptional(request.description()));
        item.setCategory(category);
        item.setStackable(request.stackable());
        item.setBuyPrice(request.buyPrice());
        item.setSellPrice(request.sellPrice());
        item.setTradable(request.tradable());
        item.setSellable(request.sellable());
        item.setUsable(request.usable());
        item.setMaxStack(Boolean.TRUE.equals(request.stackable()) ? request.maxStack() : null);
        item.setRarity(rarity);
        item.setIcon(normalizeOptional(request.icon()));

        return ItemDefinitionResponse.from(itemDefinitionRepository.save(item));
    }

    private String normalizeNameRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " é obrigatório");
        }
        return value.trim();
    }

    private String normalizeCodeRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " é obrigatório");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
