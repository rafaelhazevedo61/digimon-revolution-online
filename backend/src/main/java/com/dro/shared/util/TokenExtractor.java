package com.dro.shared.util;

import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;

import java.util.Map;
import java.util.UUID;

public class TokenExtractor {

    private TokenExtractor() {
    }

    public static UUID extractPlayerId(String token) {
        Map<String, Object> claims = JwtTokenCodec.validateAndReadClaims(
                token,
                JwtSettings.getSecret(),
                JwtSettings.getIssuer()
        );

        Object subject = claims.get("sub");
        if (!(subject instanceof String playerId) || playerId.isBlank()) {
            throw new BadRequestException("Invalid JWT subject");
        }

        try {
            return UUID.fromString(playerId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid JWT subject");
        }
    }
}