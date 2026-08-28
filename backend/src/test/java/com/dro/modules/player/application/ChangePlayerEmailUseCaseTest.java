package com.dro.modules.player.application;

import com.dro.modules.auth.domain.exception.EmailAlreadyRegisteredException;
import com.dro.modules.auth.domain.exception.InvalidCredentialsException;
import com.dro.modules.player.api.dto.request.ChangeEmailRequest;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtService;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangePlayerEmailUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ChangePlayerEmailUseCase useCase;

    private String makeToken(UUID playerId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    private Player makePlayer(UUID id) {
        return Player.builder()
                .id(id)
                .username("tamer")
                .email("old@example.com")
                .password("encoded-password")
                .build();
    }

    @Test
    void execute_changesEmailNormalizesValueAndRenewsToken() {
        UUID playerId = UUID.randomUUID();
        Player player = makePlayer(playerId);
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("current-pass", "encoded-password")).thenReturn(true);
        when(playerRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(jwtService.generateToken(player)).thenReturn("new-token");

        var response = useCase.execute(token, new ChangeEmailRequest("current-pass", "  New@Example.COM "));

        assertEquals("new@example.com", player.getEmail());
        assertEquals(1, player.getTokenVersion());
        assertEquals("new@example.com", response.email());
        assertEquals("new-token", response.token());
        verify(playerRepository).save(player);
        verify(jwtService).generateToken(player);
    }

    @Test
    void execute_throwsInvalidCredentials_whenCurrentPasswordWrong() {
        UUID playerId = UUID.randomUUID();
        Player player = makePlayer(playerId);
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("wrong-pass", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(
                token,
                new ChangeEmailRequest("wrong-pass", "new@example.com")
        ));

        verifyNoInteractions(jwtService);
    }

    @Test
    void execute_throwsConflict_whenNewEmailAlreadyExistsIgnoringCase() {
        UUID playerId = UUID.randomUUID();
        Player player = makePlayer(playerId);
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("current-pass", "encoded-password")).thenReturn(true);
        when(playerRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class, () -> useCase.execute(
                token,
                new ChangeEmailRequest("current-pass", "taken@EXAMPLE.com")
        ));

        assertEquals("old@example.com", player.getEmail());
        verifyNoInteractions(jwtService);
    }

    @Test
    void execute_throwsBadRequest_whenNewEmailEqualsCurrent() {
        UUID playerId = UUID.randomUUID();
        Player player = makePlayer(playerId);
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("current-pass", "encoded-password")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> useCase.execute(
                token,
                new ChangeEmailRequest("current-pass", " OLD@EXAMPLE.COM ")
        ));

        verifyNoInteractions(jwtService);
    }

    @Test
    void execute_throwsNotFound_whenPlayerMissing() {
        UUID playerId = UUID.randomUUID();
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> useCase.execute(
                token,
                new ChangeEmailRequest("current-pass", "new@example.com")
        ));

        verifyNoInteractions(passwordEncoder, jwtService);
    }
}
