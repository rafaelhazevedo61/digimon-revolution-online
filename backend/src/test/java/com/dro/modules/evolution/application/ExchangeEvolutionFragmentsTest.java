package com.dro.modules.evolution.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.evolution.domain.*;
import com.dro.modules.evolution.infra.*;
import com.dro.modules.inventory.application.*;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.*;
import com.dro.modules.player.domain.*;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ExchangeEvolutionFragmentsTest {
    @ParameterizedTest
    @CsvSource({"BABY,BABY_II,fragment_baby2,5", "BABY_II,ROOKIE,fragment_rookie_specific,10",
            "ROOKIE,CHAMPION,fragment_champion_specific,20", "CHAMPION,ULTIMATE,fragment_ultimate_specific,30",
            "ULTIMATE,MEGA,fragment_mega_specific,50"})
    void respectsReceivedQuotaAndAdminBypass(Stage currentStage, Stage nextStage, String icon, int limit) {
        PlayerRepository players = mock(PlayerRepository.class);
        DigimonRepository digimons = mock(DigimonRepository.class);
        EvolutionLineRepository lines = mock(EvolutionLineRepository.class);
        InventoryRepository inventory = mock(InventoryRepository.class);
        ItemDefinitionRepository definitions = mock(ItemDefinitionRepository.class);
        ConsumeItemUseCase consume = mock(ConsumeItemUseCase.class);
        AddItemUseCase grant = mock(AddItemUseCase.class);
        FragmentExchangeDailyRepository daily = mock(FragmentExchangeDailyRepository.class);
        ExchangeEvolutionFragments service = new ExchangeEvolutionFragments(players, digimons, lines,
                inventory, definitions, consume, grant, daily);
        UUID playerId = UUID.randomUUID(), digimonId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).activeDigimonId(digimonId).build();
        Digimon digimon = mock(Digimon.class);
        when(digimon.getPlayerId()).thenReturn(playerId);
        when(digimon.getStage()).thenReturn(currentStage);
        when(digimon.getDigimonInfoId()).thenReturn(1L);
        when(players.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(digimons.findById(digimonId)).thenReturn(Optional.of(digimon));
        EvolutionLine line = mock(EvolutionLine.class);
        EvolutionLineStep current = mock(EvolutionLineStep.class), next = mock(EvolutionLineStep.class);
        DigimonInfos info = mock(DigimonInfos.class);
        when(info.getId()).thenReturn(1L);
        when(current.getDigimonInfo()).thenReturn(info);
        when(current.getStepOrder()).thenReturn(1);
        when(next.getStepOrder()).thenReturn(2);
        when(next.getStage()).thenReturn(nextStage);
        EvolutionStepMaterial material = mock(EvolutionStepMaterial.class);
        when(material.getMaterialCode()).thenReturn("FRAGMENT_TARGET");
        when(next.getMaterials()).thenReturn(Set.of(material));
        when(line.getSteps()).thenReturn(new ArrayList<>(List.of(current, next)));
        when(lines.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(line));
        ItemDefinition target = ItemDefinition.builder().id(1L).code("FRAGMENT_TARGET")
                .name("Alvo").category("EVOLUTION_MATERIAL").icon(icon).build();
        ItemDefinition input = ItemDefinition.builder().id(2L).code("FRAGMENT_INPUT")
                .name("Entrada").category("EVOLUTION_MATERIAL").icon(icon).build();
        when(definitions.findByCode("FRAGMENT_TARGET")).thenReturn(Optional.of(target));
        when(definitions.findByCode("FRAGMENT_INPUT")).thenReturn(Optional.of(input));
        when(daily.received(eq(playerId), eq(nextStage), any())).thenReturn(limit - 1);

        assertEquals(1, service.exchange(playerId, digimonId, 1L, Map.of("FRAGMENT_INPUT", 3)).quantity());
        verify(grant).addMaterialToPlayer(playerId, target, 1);
        clearInvocations(consume, grant, daily);
        when(daily.received(eq(playerId), eq(nextStage), any())).thenReturn(limit);
        assertThrows(BadRequestException.class,
                () -> service.exchange(playerId, digimonId, 1L, Map.of("FRAGMENT_INPUT", 3)));
        verifyNoInteractions(consume, grant);

        player.setUserType(UserType.ADMIN);
        assertEquals(limit + 1, service.exchange(playerId, digimonId, 1L,
                Map.of("FRAGMENT_INPUT", (limit + 1) * 3)).quantity());
        verify(grant).addMaterialToPlayer(playerId, target, limit + 1);
    }
}
