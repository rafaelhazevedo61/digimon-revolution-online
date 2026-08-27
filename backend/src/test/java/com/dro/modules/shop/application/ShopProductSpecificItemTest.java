package com.dro.modules.shop.application;

import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.shop.api.dto.request.CreateShopProductRequest;
import com.dro.modules.shop.api.dto.request.UpdateShopProductRequest;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import com.dro.modules.shop.infra.ShopProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopProductSpecificItemTest {

    @Mock private ShopProductRepository shopProductRepository;
    @Mock private EquipmentTemplateRepository equipmentTemplateRepository;
    @Mock private ItemDefinitionRepository itemDefinitionRepository;

    @InjectMocks private CreateShopProductUseCase createUseCase;
    @InjectMocks private UpdateShopProductUseCase updateUseCase;

    @Test
    void createsSpecificItemUsingDefinitionCodeWhenGenericTypeIsOmitted() {
        ItemDefinition definition = definition();
        when(shopProductRepository.existsById("INCUBATION_SLOT_UNLOCK")).thenReturn(false);
        when(itemDefinitionRepository.findByCode("INCUBATION_SLOT_UNLOCK")).thenReturn(Optional.of(definition));

        var response = createUseCase.execute(new CreateShopProductRequest(
                "INCUBATION_SLOT_UNLOCK",
                "Expansor de Slot de Incubação",
                "Desbloqueia mais um slot.",
                ShopProductType.ITEM,
                ShopProductCategory.CONSUMABLE,
                null,
                "INCUBATION_SLOT_UNLOCK",
                null,
                10000,
                500
        ));

        verify(shopProductRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(product ->
                product.getItemType() == ItemType.INCUBATION_SLOT_UNLOCK
                        && "INCUBATION_SLOT_UNLOCK".equals(product.getItemDefinitionCode())
                        && product.getPrice() == 10000
        ));
        assertEquals(ItemType.INCUBATION_SLOT_UNLOCK, response.itemType());
        assertEquals("INCUBATION_SLOT_UNLOCK", response.itemDefinitionCode());
    }

    @Test
    void updatesSpecificItemUsingDefinitionCodeWhenGenericTypeIsOmitted() {
        ItemDefinition definition = definition();
        ShopProductEntity product = ShopProductEntity.builder()
                .code("INCUBATION_SLOT_UNLOCK")
                .name("Expansor antigo")
                .productType(ShopProductType.ITEM)
                .category(ShopProductCategory.CONSUMABLE)
                .itemType(null)
                .itemDefinitionCode("INCUBATION_SLOT_UNLOCK")
                .price(2000)
                .sellPrice(500)
                .active(true)
                .build();
        when(shopProductRepository.findById("INCUBATION_SLOT_UNLOCK")).thenReturn(Optional.of(product));
        when(itemDefinitionRepository.findByCode("INCUBATION_SLOT_UNLOCK")).thenReturn(Optional.of(definition));
        when(shopProductRepository.save(product)).thenReturn(product);

        var response = updateUseCase.execute("INCUBATION_SLOT_UNLOCK", new UpdateShopProductRequest(
                "Expansor de Slot de Incubação",
                "Desbloqueia mais um slot.",
                ShopProductType.ITEM,
                ShopProductCategory.CONSUMABLE,
                null,
                "INCUBATION_SLOT_UNLOCK",
                null,
                10000,
                500
        ));

        assertEquals(ItemType.INCUBATION_SLOT_UNLOCK, product.getItemType());
        assertEquals(10000, product.getPrice());
        assertEquals(ItemType.INCUBATION_SLOT_UNLOCK, response.itemType());
        verify(shopProductRepository).save(product);
    }

    private ItemDefinition definition() {
        return ItemDefinition.builder()
                .id(127L)
                .code("INCUBATION_SLOT_UNLOCK")
                .name("Expansor de Slot de Incubação")
                .category("CONSUMABLE")
                .build();
    }
}
