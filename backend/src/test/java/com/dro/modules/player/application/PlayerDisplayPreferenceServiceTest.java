package com.dro.modules.player.application;

import com.dro.modules.player.domain.PlayerDisplayPreference;
import com.dro.modules.player.infra.PlayerDisplayPreferenceRepository;
import com.dro.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerDisplayPreferenceServiceTest {

    @Mock
    private PlayerDisplayPreferenceRepository repository;

    @Test
    void getShortcutRoutes_returnsDefaultForNewPlayer() {
        UUID playerId = UUID.randomUUID();
        when(repository.findById(playerId)).thenReturn(Optional.empty());

        var service = new PlayerDisplayPreferenceService(repository);

        assertEquals(List.of("dashboard", "missions", "inventory", "shop"), service.getShortcutRoutes(playerId));
    }

    @Test
    void setShortcutRoutes_rejectsMoreThanEightRoutes() {
        var service = new PlayerDisplayPreferenceService(repository);

        assertThrows(BadRequestException.class, () -> service.setShortcutRoutes(
                UUID.randomUUID(),
                List.of("dashboard", "missions", "inventory", "shop", "forge", "mail", "arena", "ranking", "clans")
        ));
    }

    @Test
    void setShortcutRoutes_deduplicatesAndPreservesOrder() {
        UUID playerId = UUID.randomUUID();
        when(repository.findById(playerId)).thenReturn(Optional.empty());
        when(repository.save(any(PlayerDisplayPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new PlayerDisplayPreferenceService(repository);

        var routes = service.setShortcutRoutes(playerId, List.of("shop", "dashboard", "shop"));

        assertEquals(List.of("shop", "dashboard"), routes);
        verify(repository).save(any(PlayerDisplayPreference.class));
    }
}

