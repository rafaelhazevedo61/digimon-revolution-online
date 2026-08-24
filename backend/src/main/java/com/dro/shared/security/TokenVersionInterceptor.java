package com.dro.shared.security;

import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ApiErrorCode;
import com.dro.shared.exception.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Map;
import java.util.UUID;

@Component
public class TokenVersionInterceptor implements HandlerInterceptor {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PASSWORD_CHANGED_MESSAGE = "Sessão expirada: a senha foi alterada. Faça login novamente.";
    private final PlayerRepository playerRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return true;
        }
        Map<String, Object> claims;
        try {
            claims = JwtTokenCodec.validateAndReadClaims(authorization, JwtSettings.getSecret(), JwtSettings.getIssuer());
        } catch (Exception e) {
            return true;
        }
        UUID playerId;
        try {
            playerId = parsePlayerId(claims.get("sub"));
        } catch (Exception e) {
            return true;
        }
        var player = playerRepository.findById(playerId);
        if (player.isEmpty()) {
            return true;
        }
        int tokenVersion = readTokenVersion(claims.get("tokenVersion"));
        if (tokenVersion != player.get().getTokenVersion()) {
            sendError(response, request);
            return false;
        }
        return true;
    }

    private UUID parsePlayerId(Object subject) {
        if (!(subject instanceof String value) || value.isBlank()) {
            throw new IllegalArgumentException("Invalid JWT subject");
        }
        return UUID.fromString(value);
    }

    private int readTokenVersion(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private void sendError(HttpServletResponse response, HttpServletRequest request) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), ApiErrorCode.UNAUTHORIZED, PASSWORD_CHANGED_MESSAGE, request.getRequestURI());
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(errorResponse));
    }

    public TokenVersionInterceptor(final PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
}
