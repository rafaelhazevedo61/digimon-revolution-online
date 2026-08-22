package com.dro.modules.inventory.application;

import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.infra.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

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
}
