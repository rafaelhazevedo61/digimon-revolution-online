package com.dro.modules.shop.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.application.GrantEquipmentUseCase;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.shop.api.dto.BuyShopProductResponse;
import com.dro.modules.shop.api.dto.request.BuyShopProductRequest;
import com.dro.modules.shop.domain.ShopProductEntity;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import com.dro.modules.shop.infra.ShopProductRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyShopProductUseCaseTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private DigimonRepository digimonRepository;
    @Mock private AddItemUseCase addItemUseCase;
    @Mock private ItemDefinitionRepository itemDefinitionRepository;
    @Mock private GrantEquipmentUseCase grantEquipmentUseCase;
    @Mock private ShopProductRepository shopProductRepository;
    @Mock private TutorialService tutorialService;
    @Mock private TransactionAuditPublisher transactionAuditPublisher;

    @InjectMocks private BuyShopProductUseCase useCase;

    @Test
    void buyChestCreditsTheSpecificChestDefinition() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String chestCode = "CHEST_FRAGMENT_ROOKIE";
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
        ShopProductEntity product = ShopProductEntity.builder()
                .code(chestCode)
                .name("Baú de Fragmentos - Rookie")
                .description("Baú de fragmentos específicos Rookie.")
                .productType(ShopProductType.ITEM)
                .category(ShopProductCategory.CHEST)
                .itemType(ItemType.LOOT_CHEST)
                .itemDefinitionCode(chestCode)
                .price(150)
                .sellPrice(0)
                .active(true)
                .build();
        ItemDefinition chestDefinition = ItemDefinition.builder()
                .id(901L)
                .code(chestCode)
                .name("Baú de Fragmentos - Rookie")
                .description("Baú de fragmentos específicos Rookie.")
                .category("CHEST")
                .stackable(true)
                .tradable(true)
                .sellable(true)
                .usable(true)
                .maxStack(999)
                .rarity("COMMON")
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(shopProductRepository.findById(chestCode)).thenReturn(Optional.of(product));
        when(itemDefinitionRepository.findByCode(chestCode)).thenReturn(Optional.of(chestDefinition));

        BuyShopProductResponse response = useCase.execute(
                token,
                new BuyShopProductRequest(chestCode, 2)
        );

        assertEquals(chestCode, response.productCode());
        assertEquals(2, response.quantity());
        assertEquals(300, response.totalPrice());
        assertEquals(700, response.remainingBits());
        verify(addItemUseCase).addMaterial(digimonId, chestDefinition, 2);
        verify(addItemUseCase, never()).execute(any(), any(), anyInt());
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
