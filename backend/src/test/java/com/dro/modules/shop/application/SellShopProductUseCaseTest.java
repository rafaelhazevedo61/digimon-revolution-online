package com.dro.modules.shop.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.application.ConsumeItemUseCase;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.shop.api.dto.request.SellShopProductRequest;
import com.dro.modules.shop.api.dto.response.SellShopProductResponse;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellShopProductUseCaseTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private DigimonRepository digimonRepository;
    @Mock private ConsumeItemUseCase consumeItemUseCase;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private ShopProductRepository shopProductRepository;
    @Mock private ItemDefinitionRepository itemDefinitionRepository;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks private SellShopProductUseCase useCase;

    @Test
    void sellsStoredEquipmentWithNullDigimonId() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        UUID equipmentId = UUID.randomUUID();
        String token = createToken(playerId);

        Player player = Player.builder()
                .id(playerId)
                .activeDigimonId(digimonId)
                .build();
        Digimon digimon = Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .bits(1000)
                .build();
        Equipment equipment = Equipment.builder()
                .id(equipmentId)
                .playerId(playerId)
                .digimonId(null)
                .name("Iron Claw")
                .slot(EquipmentSlot.WEAPON)
                .rarity(EquipmentRarity.COMMON)
                .bonusAttack(10)
                .createdAt(LocalDateTime.now())
                .equipped(false)
                .build();
        ShopProductEntity product = ShopProductEntity.builder()
                .code("IRON_CLAW")
                .name("Iron Claw")
                .description("Arma comum")
                .productType(ShopProductType.EQUIPMENT)
                .category(ShopProductCategory.EQUIPMENT)
                .equipmentTemplateName("Iron Claw")
                .price(250)
                .sellPrice(60)
                .active(true)
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(shopProductRepository.findByEquipmentTemplateNameIgnoreCase("Iron Claw"))
                .thenReturn(Optional.of(product));

        SellShopProductResponse response = useCase.execute(
                token,
                new SellShopProductRequest(null, equipmentId, null, 1)
        );

        assertEquals("IRON_CLAW", response.productCode());
        assertEquals(1, response.quantity());
        assertEquals(60, response.totalSellPrice());
        assertEquals(1060, response.remainingBits());
        verify(equipmentRepository).delete(equipment);
        verify(digimonRepository).save(digimon);
    }

    private static String createToken(UUID subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}
