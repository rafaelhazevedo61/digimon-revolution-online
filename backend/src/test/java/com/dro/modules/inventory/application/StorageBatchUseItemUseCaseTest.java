package com.dro.modules.inventory.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.api.dto.response.UseItemResponse;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StorageBatchUseItemUseCaseTest {

    @Test
    void consumesMultipleStoragePlusFiveItemsAndAppliesTotalExpansion() {
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        DigimonRepository digimonRepository = mock(DigimonRepository.class);
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        UseItemUseCase useCase = new UseItemUseCase(inventoryRepository, digimonRepository, playerRepository);

        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .activeDigimonId(digimonId)
                .maxStorageSlots(50)
                .build();
        Digimon digimon = Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .level(10)
                .build();
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .itemType(ItemType.STORAGE_SLOT_5)
                .quantity(6)
                .build();

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findByIdForUpdate(digimonId)).thenReturn(Optional.of(digimon));
        when(inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.STORAGE_SLOT_5))
                .thenReturn(Optional.of(item));

        UseItemResponse response = useCase.execute(tokenFor(playerId), ItemType.STORAGE_SLOT_5, 4);

        assertThat(response.quantity()).isEqualTo(4);
        assertThat(response.message()).isEqualTo("Storage expandido em +20 espaço(s)!");
        assertThat(player.getMaxStorageSlots()).isEqualTo(70);
        assertThat(item.getQuantity()).isEqualTo(2);
        verify(inventoryRepository).save(item);
        verify(playerRepository).save(player);
    }

    private String tokenFor(UUID playerId) {
        return JwtTokenCodec.create(
                Map.of(
                        "sub", playerId.toString(),
                        "iss", JwtSettings.getIssuer(),
                        "exp", Instant.now().getEpochSecond() + 3600
                ),
                JwtSettings.getSecret()
        );
    }
}
