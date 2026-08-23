package com.dro.modules.player.application;

import com.dro.modules.player.api.dto.request.ResetPlayerPasswordRequest;
import com.dro.modules.player.api.dto.response.ResetPlayerPasswordResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.AdminAuditService;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPlayerPasswordUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminAuditService adminAuditService;

    @InjectMocks
    private ResetPlayerPasswordUseCase useCase;

    @Test
    void execute_resetsPasswordWithProvidedValue() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .username("tamer")
                .password("old-encoded")
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(passwordEncoder.encode("new-password")).thenReturn("new-encoded");

        ResetPlayerPasswordResponse response = useCase.execute(
                "Bearer token",
                playerId,
                new ResetPlayerPasswordRequest("new-password", false)
        );

        assertEquals(playerId, response.playerId());
        assertEquals("tamer", response.username());
        assertEquals("new-password", response.newPassword());
        assertFalse(response.generated());
        assertEquals("new-encoded", player.getPassword());
        assertEquals(1, player.getTokenVersion());
        verify(playerRepository).save(player);
    }

    @Test
    void execute_generatesRandomPassword() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .username("tamer")
                .password("old-encoded")
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(passwordEncoder.encode(any(String.class))).thenReturn("generated-encoded");

        ResetPlayerPasswordResponse response = useCase.execute(
                "Bearer token",
                playerId,
                new ResetPlayerPasswordRequest(null, true)
        );

        assertEquals(playerId, response.playerId());
        assertTrue(response.generated());
        assertNotNull(response.newPassword());
        assertEquals(12, response.newPassword().length());
        assertEquals(1, player.getTokenVersion());
        verify(playerRepository).save(player);
    }

    @Test
    void execute_throwsNotFound_whenPlayerMissing() {
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.execute(
                "Bearer token",
                playerId,
                new ResetPlayerPasswordRequest("new-password", false)
        ));
    }

    @Test
    void execute_throwsBadRequest_whenManualPasswordTooShort() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .username("tamer")
                .password("old-encoded")
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(BadRequestException.class, () -> useCase.execute(
                "Bearer token",
                playerId,
                new ResetPlayerPasswordRequest("abc", false)
        ));
    }

    @Test
    void execute_throwsBadRequest_whenManualPasswordMissing() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .username("tamer")
                .password("old-encoded")
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(BadRequestException.class, () -> useCase.execute(
                "Bearer token",
                playerId,
                new ResetPlayerPasswordRequest(null, false)
        ));
    }
}
