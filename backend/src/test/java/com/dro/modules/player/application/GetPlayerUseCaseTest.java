package com.dro.modules.player.application;
import com.dro.shared.security.JwtTestToken;

import com.dro.modules.player.api.dto.response.PlayerResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPlayerUseCaseTest {

    @Mock
    private PlayerRepository repository;

    @InjectMocks
    private GetPlayerUseCase getPlayerUseCase;

    @Test
    void execute_returnsPlayerResponse_whenTokenValid() {
        UUID playerId = UUID.randomUUID();
        String token = JwtTestToken.create(playerId);

        Player player = Player.builder()
                .id(playerId)
                .username("testuser")
                .email("test@email.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(playerId)).thenReturn(Optional.of(player));

        PlayerResponse response = getPlayerUseCase.execute(token);

        assertEquals(playerId, response.id());
        assertEquals("testuser", response.username());
        assertEquals("test@email.com", response.email());
    }

    @Test
    void execute_throwsException_whenTokenNull() {
        assertThrows(RuntimeException.class, () -> getPlayerUseCase.execute(null));
    }

    @Test
    void execute_throwsException_whenTokenInvalid() {
        assertThrows(RuntimeException.class, () -> getPlayerUseCase.execute("invalidtoken"));
    }

    @Test
    void execute_throwsException_whenPlayerNotFound() {
        UUID playerId = UUID.randomUUID();
        String token = JwtTestToken.create(playerId);

        when(repository.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> getPlayerUseCase.execute(token));
    }
}
