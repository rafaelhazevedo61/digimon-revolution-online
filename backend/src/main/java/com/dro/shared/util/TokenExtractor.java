package com.dro.shared.util;

import com.dro.shared.exception.BadRequestException;

import java.util.UUID;

public class TokenExtractor {

    private TokenExtractor() {
    }

    public static UUID extractPlayerId(String token) {
        if (token == null || !token.contains(":")) {
            throw new BadRequestException("Invalid token");
        }
        return UUID.fromString(token.split(":")[1]);
    }
}
