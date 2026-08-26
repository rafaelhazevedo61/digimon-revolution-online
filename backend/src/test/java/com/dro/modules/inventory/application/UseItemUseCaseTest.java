package com.dro.modules.inventory.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UseItemUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private UseItemUseCase useItemUseCase;

    @Test
    void rejectsDigitamaThroughGenericUseEndpoint() {
        assertThatThrownBy(() -> useItemUseCase.execute("unused-token", ItemType.DIGITAMA_FIRE))
                .isInstanceOf(com.dro.shared.exception.BadRequestException.class)
                .hasMessageContaining("tela de incubação");

        verifyNoInteractions(inventoryRepository, digimonRepository, playerRepository);
    }

    @Test
    void rejectsIncubatorThroughGenericUseEndpoint() {
        assertThatThrownBy(() -> useItemUseCase.execute("unused-token", ItemType.INCUBATOR_EPIC))
                .isInstanceOf(com.dro.shared.exception.BadRequestException.class)
                .hasMessageContaining("tela de incubação");

        verifyNoInteractions(inventoryRepository, digimonRepository, playerRepository);
    }

    @Test
    void consumesUnlockItemAndIncrementsPlayerCapacity() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = createPlayer(playerId, digimonId, 1);
        Digimon digimon = mock(Digimon.class);
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .itemType(ItemType.INCUBATION_SLOT_UNLOCK)
                .quantity(1)
                .build();

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(digimon.getId()).thenReturn(digimonId);
        when(inventoryRepository.findByDigimonIdAndItemTypeForUpdate(digimonId, ItemType.INCUBATION_SLOT_UNLOCK))
                .thenReturn(Optional.of(item));

        useItemUseCase.execute(tokenFor(playerId), ItemType.INCUBATION_SLOT_UNLOCK);

        assertThat(player.getUnlockedIncubationSlots()).isEqualTo(2);
        assertThat(item.getQuantity()).isZero();
        verify(inventoryRepository).delete(item);
        verify(playerRepository).save(player);
        verify(digimonRepository, never()).save(any(Digimon.class));
    }

    @Test
    void rejectsUnlockWhenPlayerAlreadyHasAllSlots() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = createPlayer(playerId, digimonId, 3);
        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> useItemUseCase.execute(tokenFor(playerId), ItemType.INCUBATION_SLOT_UNLOCK))
                .isInstanceOf(com.dro.shared.exception.BadRequestException.class)
                .hasMessageContaining("já estão desbloqueados");

        verifyNoInteractions(inventoryRepository, digimonRepository);
    }

    @Test
    void requiresActiveDigimonOnlyToLocateTheItemAndKeepsCapacityOnPlayer() {
        UUID playerId = UUID.randomUUID();
        Player player = createPlayer(playerId, null, 1);
        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> useItemUseCase.execute(tokenFor(playerId), ItemType.INCUBATION_SLOT_UNLOCK))
                .isInstanceOf(com.dro.shared.exception.BadRequestException.class)
                .hasMessageContaining("No active digimon selected");

        assertThat(player.getUnlockedIncubationSlots()).isEqualTo(1);
        verifyNoInteractions(inventoryRepository, digimonRepository);
    }

    private Player createPlayer(UUID playerId, UUID digimonId, int unlockedSlots) {
        Player player = Player.createPlayer(
                playerId,
                "player-" + playerId,
                playerId + "@example.com",
                "password",
                LocalDateTime.now()
        );
        player.setActiveDigimonId(digimonId);
        player.setUnlockedIncubationSlots(unlockedSlots);
        return player;
    }

    private String tokenFor(UUID playerId) {
        return JwtTokenCodec.create(
                Map.of(
                        "sub", playerId.toString(),
                        "iss", JwtSettings.getIssuer(),
                        "exp", Instant.now().plusSeconds(300).getEpochSecond()
                ),
                JwtSettings.getSecret()
        );
    }
}
