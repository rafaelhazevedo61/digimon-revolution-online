package com.dro.modules.inventory.application;

import com.dro.modules.inventory.api.dto.request.UpdateItemDefinitionRequest;
import com.dro.modules.inventory.api.dto.response.ItemDefinitionPageResponse;
import com.dro.modules.inventory.api.dto.response.ItemDefinitionResponse;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemDefinitionAdminUseCaseTest {

    @Mock private ItemDefinitionRepository itemDefinitionRepository;

    @InjectMocks private GetItemDefinitionsUseCase getItemDefinitionsUseCase;
    @InjectMocks private UpdateItemDefinitionUseCase updateItemDefinitionUseCase;

    @Test
    void catalogSearchIsNormalizedAndPassedToRepository() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        ItemDefinition item = baseItem();
        when(itemDefinitionRepository.findCatalog(
                eq("BAU"), eq(null), eq(null), eq(null), eq(null), eq(null), eq(pageRequest)
        )).thenReturn(new PageImpl<>(List.of(item), pageRequest, 1));

        ItemDefinitionPageResponse response = getItemDefinitionsUseCase.execute(
                "  bau ", null, null, null, null, null, pageRequest
        );

        assertEquals(1, response.totalItems());
        assertEquals("CHEST_FRAGMENT_ROOKIE", response.items().get(0).code());
        verify(itemDefinitionRepository).findCatalog(
                "BAU", null, null, null, null, null, pageRequest
        );
    }

    @Test
    void catalogWithoutSearchUsesTypedEmptyString() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(itemDefinitionRepository.findCatalog(
                eq(""), eq(null), eq(null), eq(null), eq(null), eq(null), eq(pageRequest)
        )).thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        ItemDefinitionPageResponse response = getItemDefinitionsUseCase.execute(
                null, null, null, null, null, null, pageRequest
        );

        assertEquals(0, response.totalItems());
        verify(itemDefinitionRepository).findCatalog(
                "", null, null, null, null, null, pageRequest
        );
    }

    @Test
    void updateChangesEditableAttributesWithoutChangingCode() {
        ItemDefinition item = baseItem();
        when(itemDefinitionRepository.findById(7L)).thenReturn(Optional.of(item));
        when(itemDefinitionRepository.save(item)).thenReturn(item);

        ItemDefinitionResponse response = updateItemDefinitionUseCase.execute(
                7L,
                new UpdateItemDefinitionRequest(
                        "Baú de Fragmentos - Rookie",
                        "Descrição atualizada.",
                        "chest",
                        true,
                        150,
                        20,
                        true,
                        true,
                        true,
                        999,
                        "common",
                        "chest_fragment_rookie"
                )
        );

        assertEquals("CHEST_FRAGMENT_ROOKIE", item.getCode());
        assertEquals("Baú de Fragmentos - Rookie", response.name());
        assertEquals("CHEST", response.category());
        assertEquals("COMMON", response.rarity());
        assertEquals(150, response.buyPrice());
        assertEquals(20, response.sellPrice());
        assertEquals(999, response.maxStack());
        verify(itemDefinitionRepository).save(item);
    }

    @Test
    void updateRejectsStackableItemAboveMaximumStack() {
        ItemDefinition item = baseItem();
        when(itemDefinitionRepository.findById(7L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> updateItemDefinitionUseCase.execute(
                7L,
                new UpdateItemDefinitionRequest(
                        "Baú de Fragmentos - Rookie",
                        null,
                        "CHEST",
                        true,
                        null,
                        null,
                        true,
                        true,
                        true,
                        1000,
                        "COMMON",
                        null
                )
        ));
    }

    @Test
    void updateRejectsStackableItemWithoutMaximumStack() {
        ItemDefinition item = baseItem();
        when(itemDefinitionRepository.findById(7L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> updateItemDefinitionUseCase.execute(
                7L,
                new UpdateItemDefinitionRequest(
                        "Baú de Fragmentos - Rookie",
                        null,
                        "CHEST",
                        true,
                        null,
                        null,
                        true,
                        true,
                        true,
                        null,
                        "COMMON",
                        null
                )
        ));
    }

    private static ItemDefinition baseItem() {
        return ItemDefinition.builder()
                .id(7L)
                .code("CHEST_FRAGMENT_ROOKIE")
                .name("Baú antigo")
                .description("Descrição antiga")
                .category("CHEST")
                .stackable(true)
                .buyPrice(100)
                .sellPrice(10)
                .tradable(true)
                .sellable(true)
                .usable(true)
                .maxStack(999)
                .rarity("COMMON")
                .icon("chest_fragment_rookie")
                .build();
    }
}
