package com.dro.shared.security;

import com.dro.modules.player.domain.Player;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    public String generateToken(Player player) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(JwtSettings.getExpirationMinutes(), ChronoUnit.MINUTES);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("sub", player.getId().toString());
        claims.put("playerId", player.getId().toString());
        claims.put("username", player.getUsername());
        claims.put("email", player.getEmail());
        claims.put("userType", player.getUserType().name());
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", expiresAt.getEpochSecond());

        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}