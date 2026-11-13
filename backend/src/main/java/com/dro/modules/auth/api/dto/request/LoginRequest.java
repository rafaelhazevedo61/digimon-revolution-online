package com.dro.modules.auth.api.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrato de dados do módulo de Autenticação.
 */
public record LoginRequest(

        @NotBlank
        @JsonAlias("email")
        String identifier,

        @NotBlank
        String password

) {}
