package com.dro.modules.achievement.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.UseItemUseCase;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UseVipPassTest {
    @Test
    void consumesOnePassAndExtendsActiveVipWithoutAnyActiveDigimon() {
        UUID id = UUID.randomUUID();
        InventoryRepository inventory = mock(InventoryRepository.class);
        DigimonRepository digimons = mock(DigimonRepository.class);
        PlayerRepository players = mock(PlayerRepository.class);
        Player player = Player.builder().id(id).build();
        Instant expiry = Instant.now().plus(Duration.ofDays(7));
        player.setVipExpiresAt(expiry);
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).playerId(id)
                .itemType(ItemType.VIP_DAY_PASS).quantity(2).build();
        when(players.findByIdForUpdate(id)).thenReturn(Optional.of(player));
        when(inventory.findByPlayerIdAndItemTypeForUpdate(id, ItemType.VIP_DAY_PASS)).thenReturn(Optional.of(item));

        var result = new UseItemUseCase(inventory, digimons, players).execute(token(id), ItemType.VIP_DAY_PASS);

        assertEquals(1, result.quantity());
        assertEquals(1, item.getQuantity());
        assertEquals(expiry.plus(Duration.ofHours(24)), player.getVipExpiresAt());
        verify(inventory).save(item);
        verify(players).save(player);
        verifyNoInteractions(digimons);
    }

    @Test
    void missingPassNeverActivatesVip() {
        UUID id = UUID.randomUUID();
        InventoryRepository inventory = mock(InventoryRepository.class);
        PlayerRepository players = mock(PlayerRepository.class);
        Player player = Player.builder().id(id).build();
        when(players.findByIdForUpdate(id)).thenReturn(Optional.of(player));
        when(inventory.findByPlayerIdAndItemTypeForUpdate(id, ItemType.VIP_DAY_PASS)).thenReturn(Optional.empty());
        assertThrows(UnprocessableException.class, () -> new UseItemUseCase(inventory, mock(DigimonRepository.class), players)
                .execute(token(id), ItemType.VIP_DAY_PASS));
        assertNull(player.getVipExpiresAt());
        verify(players, never()).save(any());
    }

    @Test
    void refusesBatchQuantityInsteadOfSilentlyConsumingOne() {
        InventoryRepository inventory = mock(InventoryRepository.class);
        PlayerRepository players = mock(PlayerRepository.class);
        assertThrows(BadRequestException.class, () -> new UseItemUseCase(inventory, mock(DigimonRepository.class), players)
                .execute(token(UUID.randomUUID()), ItemType.VIP_DAY_PASS, 2));
        verifyNoInteractions(inventory, players);
    }

    private static String token(UUID id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", id.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}
