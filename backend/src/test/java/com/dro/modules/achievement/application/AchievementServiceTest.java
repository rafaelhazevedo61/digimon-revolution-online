package com.dro.modules.achievement.application;

import com.dro.modules.achievement.api.AchievementDtos.Reward;
import com.dro.modules.achievement.infra.AchievementRepository;
import com.dro.modules.achievement.infra.AchievementRepository.*;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.audit.TransactionAuditPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {
    @Mock AchievementRepository repository;
    @Mock PlayerRepository players;
    @Mock ItemDefinitionRepository items;
    @Mock AddItemUseCase grant;
    @Mock TransactionAuditPublisher audit;
    AchievementService service;
    final UUID id = UUID.randomUUID();
    final Reward reward = new Reward(300, "ENERGY_CELL", "Célula de Energia", 2);
    final Definition definition = new Definition("MISSION_10", "MISSOES", "Explorador", "Resgate 10 missões.", "MISSIONS", 10, reward);
    Player player;

    @BeforeEach
    void setup() {
        service = new AchievementService(repository, players, items, grant, audit);
        player = Player.builder().id(id).bits(100).build();
        when(players.findByIdForUpdate(id)).thenReturn(Optional.of(player));
        when(repository.definitions()).thenReturn(List.of(definition));
        when(repository.metrics(id)).thenReturn(Map.of("MISSIONS", 10L));
    }

    @Test
    void claimCreditsAccountAndInventoryWithoutActiveDigimon() {
        when(repository.progress(id)).thenReturn(Map.of(definition.code(), new Progress(10, Instant.now(), null)));
        when(repository.claim(id, definition.code())).thenReturn(true);
        ItemDefinition item = ItemDefinition.builder().id(1L).code("ENERGY_CELL").build();
        when(items.findByCode("ENERGY_CELL")).thenReturn(Optional.of(item));

        var result = service.claim(id, definition.code());

        assertEquals(400, player.getBits());
        assertEquals(reward, result.reward());
        verify(grant).addMaterialToPlayer(id, item, 2);
        verify(players).save(player);
    }

    @Test
    void repeatedClaimDoesNotGiveAnyReward() {
        when(repository.progress(id)).thenReturn(Map.of(definition.code(), new Progress(10, Instant.now(), Instant.now())));
        assertThrows(ConflictException.class, () -> service.claim(id, definition.code()));
        verifyNoInteractions(grant, items);
        verify(repository, never()).claim(any(), any());
        assertEquals(100, player.getBits());
    }

    @Test
    void unfinishedAchievementDoesNotGiveAnyReward() {
        when(repository.progress(id)).thenReturn(Map.of(definition.code(), new Progress(9, null, null)));
        assertThrows(BadRequestException.class, () -> service.claim(id, definition.code()));
        verifyNoInteractions(grant, items);
    }

    @Test
    void bitsOverflowRejectsClaimBeforeChangingAnything() {
        player.setBits(Integer.MAX_VALUE - 1);
        when(repository.progress(id)).thenReturn(Map.of(definition.code(), new Progress(10, Instant.now(), null)));
        when(items.findByCode("ENERGY_CELL")).thenReturn(Optional.of(ItemDefinition.builder().id(1L).build()));
        assertThrows(BadRequestException.class, () -> service.claim(id, definition.code()));
        verify(repository, never()).claim(any(), any());
        verifyNoInteractions(grant);
    }

    @Test
    void failedItemGrantDoesNotCreditBits() {
        when(repository.progress(id)).thenReturn(Map.of(definition.code(), new Progress(10, Instant.now(), null)));
        when(repository.claim(id, definition.code())).thenReturn(true);
        ItemDefinition item = ItemDefinition.builder().id(1L).build();
        when(items.findByCode("ENERGY_CELL")).thenReturn(Optional.of(item));
        doThrow(new BadRequestException("Stack cheio")).when(grant).addMaterialToPlayer(id, item, 2);
        assertThrows(BadRequestException.class, () -> service.claim(id, definition.code()));
        assertEquals(100, player.getBits());
        verify(players, never()).save(any());
    }

    @Test
    void listShowsPermanentCompletionEvenIfCurrentObservationIsLower() {
        when(repository.metrics(id)).thenReturn(Map.of("MISSIONS", 0L));
        when(repository.progress(id)).thenReturn(Map.of(definition.code(), new Progress(10, Instant.now(), null)));
        var page = service.list(id);
        assertEquals(1, page.summary().available());
        assertEquals("AVAILABLE", page.achievements().get(0).status());
        assertEquals(10, page.achievements().get(0).progress());
    }
}
