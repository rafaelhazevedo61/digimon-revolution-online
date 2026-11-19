package com.dro.modules.invite.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateAlphaInviteRequest(
        @NotBlank @Size(max = 100) String testerName,
        @NotBlank @Email @Size(max = 100) String testerEmail,
        @NotNull @Future LocalDateTime expiresAt
) {
}
