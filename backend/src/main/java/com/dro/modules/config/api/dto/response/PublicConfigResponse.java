package com.dro.modules.config.api.dto.response;

public record PublicConfigResponse(
        boolean registrationInviteRequired,
        String appVersion
) {
}