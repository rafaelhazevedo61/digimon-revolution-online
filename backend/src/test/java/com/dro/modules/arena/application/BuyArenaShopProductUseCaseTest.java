package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.request.BuyArenaShopRequest;
import com.dro.modules.arena.api.dto.response.BuyArenaShopResponse;
import com.dro.modules.arena.domain.ArenaShopProduct;
import com.dro.modules.arena.infra.ArenaShopProductRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonGrade;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyArenaShopProductUseCaseTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private DigimonRepository digimonRepository;
    @Mock private ArenaShopProductRepository arenaShopProductRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private AddItemUseCase addItemUseCase;

    @InjectMocks private BuyArenaShopProductUseCase useCase;

    private UUID playerId;
    private UUID digimonId;
    private String token;
    private Player player;
    private Digimon digimon;
    private ArenaShopProduct product;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = createToken(playerId);

        player = Player.builder()
                .id(playerId)
                .username("buyer")
                .email("buyer@test.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .activeDigimonId(digimonId)
                .userType(UserType.PLAYER)
                .arenaCoins(100)
                .build();

        digimon = Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .name("Agumon")
                .type("FIRE")
                .stage(Stage.ROOKIE)
                .level(10)
                .experience(0)
                .hp(100).attack(50).defense(50)
                .grade(DigimonGrade.C)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(100).maxEnergy(100)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(0)
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        product = ArenaShopProduct.builder()
                .code("ARENA_POTION_SMALL")
                .name("Poção Pequena")
                .itemType(ItemType.POTION_SMALL)
                .quantity(1)
                .priceCoins(20)
                .active(true)
                .build();
    }

    private static String createToken(UUID subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    @Test
    void buyDebitsCoinsAndGrantsItems() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(arenaShopProductRepository.findById("ARENA_POTION_SMALL")).thenReturn(Optional.of(product));

        BuyArenaShopResponse response = useCase.execute(token, new BuyArenaShopRequest("ARENA_POTION_SMALL", 3));

        assertEquals(60, response.totalPrice());
        assertEquals(3, response.quantity());
        assertEquals(40, response.arenaCoinsBalance());
        assertEquals(40, player.getArenaCoins());
        verify(addItemUseCase).execute(digimonId, ItemType.POTION_SMALL, 3);
        verify(playerRepository).save(player);
    }

    @Test
    void buyAllowsOnlyRemainingStackQuantity() {
        InventoryItem existing = InventoryItem.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .itemType(ItemType.POTION_SMALL)
                .quantity(998)
                .build();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(arenaShopProductRepository.findById("ARENA_POTION_SMALL")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.POTION_SMALL))
                .thenReturn(Optional.of(existing));

        BuyArenaShopResponse response = useCase.execute(token, new BuyArenaShopRequest("ARENA_POTION_SMALL", 1));

        assertEquals(1, response.quantity());
        verify(addItemUseCase).execute(digimonId, ItemType.POTION_SMALL, 1);
    }

    @Test
    void buyFailsWhenQuantityExceedsRemainingStack() {
        InventoryItem existing = InventoryItem.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .itemType(ItemType.POTION_SMALL)
                .quantity(998)
                .build();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(arenaShopProductRepository.findById("ARENA_POTION_SMALL")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.POTION_SMALL))
                .thenReturn(Optional.of(existing));

        assertThrows(com.dro.shared.exception.BadRequestException.class,
                () -> useCase.execute(token, new BuyArenaShopRequest("ARENA_POTION_SMALL", 2)));
        verify(addItemUseCase, never()).execute(any(), any(), anyInt());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void buyFailsWhenNotEnoughCoins() {
        player.setArenaCoins(10);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(arenaShopProductRepository.findById("ARENA_POTION_SMALL")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.POTION_SMALL))
                .thenReturn(Optional.empty());

        assertThrows(UnprocessableException.class,
                () -> useCase.execute(token, new BuyArenaShopRequest("ARENA_POTION_SMALL", 1)));
        verify(addItemUseCase, never()).execute(any(), any(), anyInt());
        verify(playerRepository, never()).save(any());
    }
}
