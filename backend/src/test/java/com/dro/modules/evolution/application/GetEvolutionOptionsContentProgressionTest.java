package com.dro.modules.evolution.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.evolution.api.dto.response.EvolutionOptionsResponse;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetEvolutionOptionsContentProgressionTest {

    @Test
    void queriesOnlyEvolutionLinesFromActiveContent() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Long digimonInfoId = 1L;

        DigimonRepository digimonRepository = mock(DigimonRepository.class);
        DigimonInfosRepository digimonInfosRepository = mock(DigimonInfosRepository.class);
        EvolutionLineRepository evolutionLineRepository = mock(EvolutionLineRepository.class);
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        ItemDefinitionRepository itemDefinitionRepository = mock(ItemDefinitionRepository.class);
        PlayerRepository playerRepository = mock(PlayerRepository.class);

        Digimon digimon = Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .digimonInfoId(digimonInfoId)
                .name("Teste")
                .type("TEST")
                .stage(Stage.ROOKIE)
                .level(15)
                .build();

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(evolutionLineRepository.findByActiveTrueAndContentActiveTrueAndSteps_DigimonInfo_Id(digimonInfoId))
                .thenReturn(List.of());
        when(digimonInfosRepository.findById(digimonInfoId)).thenReturn(Optional.empty());

        EvolutionOptionsResponse response = new GetEvolutionOptionsUseCase(
                digimonRepository,
                digimonInfosRepository,
                evolutionLineRepository,
                inventoryRepository,
                itemDefinitionRepository,
                playerRepository
        ).execute(JwtTestToken.create(playerId), digimonId);

        assertTrue(response.options().isEmpty());
        verify(evolutionLineRepository).findByActiveTrueAndContentActiveTrueAndSteps_DigimonInfo_Id(digimonInfoId);
        verify(evolutionLineRepository, never()).findByActiveTrueAndSteps_DigimonInfo_Id(digimonInfoId);
    }
}
