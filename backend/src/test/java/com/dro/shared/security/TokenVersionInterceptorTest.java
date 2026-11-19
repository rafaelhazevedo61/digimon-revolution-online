package com.dro.shared.security;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenVersionInterceptorTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUpResponseWriter() throws Exception {
        lenient().when(request.getRequestURI()).thenReturn("/players/me");
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void allowsTokenWhenVersionMatches() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(token(playerId, 2));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player(playerId, 2)));

        assertThat(new TokenVersionInterceptor(playerRepository).preHandle(request, response, null)).isTrue();
    }

    @Test
    void rejectsTokenWhenVersionDiffers() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(token(playerId, 1));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player(playerId, 2)));

        assertThat(new TokenVersionInterceptor(playerRepository).preHandle(request, response, null)).isFalse();
        verify(response).setStatus(401);
    }

    @Test
    void allowsTokenWithoutVersionWhenPlayerVersionIsZero() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(tokenWithoutVersion(playerId));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player(playerId, 0)));

        assertThat(new TokenVersionInterceptor(playerRepository).preHandle(request, response, null)).isTrue();
    }

    @Test
    void allowsRequestWithoutAuthorizationHeader() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThat(new TokenVersionInterceptor(playerRepository).preHandle(request, response, null)).isTrue();
    }

    @Test
    void allowsInvalidToken() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");

        assertThat(new TokenVersionInterceptor(playerRepository).preHandle(request, response, null)).isTrue();
    }

    private Player player(UUID id, int tokenVersion) {
        return Player.builder()
                .id(id)
                .username("tamer")
                .tokenVersion(tokenVersion)
                .build();
    }

    private String token(UUID playerId, int tokenVersion) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("tokenVersion", tokenVersion);
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    private String tokenWithoutVersion(UUID playerId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}
