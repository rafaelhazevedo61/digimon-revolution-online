package com.dro.modules.auth.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Contrato de dados do módulo de Autenticação.
 */
public record RegisterRequest(

        @NotBlank
        String username,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(
                min = 8,
                max = 60,
                message = "A senha deve ter entre 8 e 60 caracteres."
        )
        String password,

        String inviteCode

) {}