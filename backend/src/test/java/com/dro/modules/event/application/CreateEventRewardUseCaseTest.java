package com.dro.modules.event.application;

import com.dro.modules.admin.api.dto.AdminEventRewardRequest;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.event.domain.EventRewardRecipientType;
import com.dro.modules.event.infra.EventRewardRepository;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEventRewardUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private EventRewardRepository eventRewardRepository;

    @Mock
    private ItemDefinitionRepository itemDefinitionRepository;

    @Mock
    private CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    @InjectMocks
    private CreateEventRewardUseCase createEventRewardUseCase;

    @Test
    void sendsGlobalRewardToAllPlayerAccountsAndExcludesAdmins() {
        List<Player> players = List.of(
                createPlayer("jogador-1", UserType.PLAYER),
                createPlayer("jogador-2", UserType.PLAYER)
        );
        when(playerRepository.findByUserTypeOrderByUsernameAsc(UserType.PLAYER)).thenReturn(players);
        when(eventRewardRepository.insertIfAbsent(
                any(UUID.class), any(UUID.class), anyString(), anyString(), anyString(), anyString(),
                anyInt(), nullable(String.class), nullable(String.class), anyInt(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(1);

        EventRewardBatchResult result = createEventRewardUseCase.execute(globalRequest());

        assertThat(result.requestedCount()).isEqualTo(2);
        assertThat(result.createdCount()).isEqualTo(2);
        assertThat(result.skippedCount()).isZero();
        verify(playerRepository).findByUserTypeOrderByUsernameAsc(UserType.PLAYER);
        verify(createSystemMailMessageUseCase, times(2)).create(
                any(), eq("EVENT_REWARD"), any(UUID.class), any(UUID.class),
                eq("EVENT_REWARD_CLAIM"), eq("Premiação global"), anyString(), anyString()
        );
    }

    @Test
    void acceptsSpecificCatalogDefinitionAndPersistsItsCode() {
        Player player = createPlayer("jogador-item", UserType.PLAYER);
        ItemDefinition definition = ItemDefinition.builder()
                .code("FRAGMENT_AGUMON")
                .name("Fragmento do Agumon")
                .category("EVOLUTION_MATERIAL")
                .stackable(true)
                .tradable(true)
                .sellable(true)
                .usable(false)
                .maxStack(999)
                .rarity("COMMON")
                .build();
        when(playerRepository.findByUsernameIgnoreCase("jogador-item")).thenReturn(Optional.of(player));
        when(itemDefinitionRepository.findByCode("FRAGMENT_AGUMON")).thenReturn(Optional.of(definition));
        when(eventRewardRepository.insertIfAbsent(
                any(UUID.class), eq(player.getId()), anyString(), anyString(), anyString(), anyString(),
                eq(0), eq("EVOLUTION_MATERIAL"), eq("FRAGMENT_AGUMON"), eq(2), anyString(),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(1);

        AdminEventRewardRequest request = new AdminEventRewardRequest(
                EventRewardRecipientType.PLAYER,
                "jogador-item",
                null,
                null,
                "EVENT",
                "evento-item-specifico",
                "Premiação de item",
                "Você recebeu um fragmento.",
                0,
                null,
                "FRAGMENT_AGUMON",
                2,
                7
        );

        EventRewardBatchResult result = createEventRewardUseCase.execute(request);

        assertThat(result.createdCount()).isEqualTo(1);
        verify(itemDefinitionRepository).findByCode("FRAGMENT_AGUMON");
        verify(eventRewardRepository).insertIfAbsent(
                any(UUID.class), eq(player.getId()), eq("EVENT"), eq("evento-item-specifico"),
                eq("Premiação de item"), anyString(), eq(0), eq("EVOLUTION_MATERIAL"),
                eq("FRAGMENT_AGUMON"), eq(2), eq("PENDING"), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    void globalRewardIsNotLimitedToOneHundredPlayers() {
        List<Player> players = java.util.stream.IntStream.rangeClosed(1, 101)
                .mapToObj(index -> createPlayer("jogador-" + index, UserType.PLAYER))
                .toList();
        when(playerRepository.findByUserTypeOrderByUsernameAsc(UserType.PLAYER)).thenReturn(players);
        when(eventRewardRepository.insertIfAbsent(
                any(UUID.class), any(UUID.class), anyString(), anyString(), anyString(), anyString(),
                anyInt(), nullable(String.class), nullable(String.class), anyInt(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(1);

        EventRewardBatchResult result = createEventRewardUseCase.execute(globalRequest());

        assertThat(result.requestedCount()).isEqualTo(101);
        assertThat(result.createdCount()).isEqualTo(101);
        assertThat(result.skippedCount()).isZero();
        verify(createSystemMailMessageUseCase, times(101)).create(
                any(), eq("EVENT_REWARD"), any(UUID.class), any(UUID.class),
                eq("EVENT_REWARD_CLAIM"), eq("Premiação global"), anyString(), anyString()
        );
    }

    private AdminEventRewardRequest globalRequest() {
        return new AdminEventRewardRequest(
                EventRewardRecipientType.ALL_PLAYERS,
                null,
                null,
                null,
                "EVENT",
                "evento-global-001",
                "Premiação global",
                "Obrigado por participar do evento.",
                100,
                null,
                null,
                0,
                7
        );
    }

    private Player createPlayer(String username, UserType userType) {
        Player player = Player.createPlayer(
                UUID.randomUUID(),
                username,
                username + "@example.com",
                "password",
                LocalDateTime.now()
        );
        player.setUserType(userType);
        return player;
    }
}
