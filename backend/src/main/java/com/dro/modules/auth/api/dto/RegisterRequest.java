package com.dro.modules.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

        @NotBlank
        String username,

        @Email
        String email,

        @NotBlank
        String password

) {}
