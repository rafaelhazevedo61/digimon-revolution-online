package com.dro.modules.admin.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminAnnouncementRequest(
        @NotBlank(message = "O assunto é obrigatório.")
        @Size(max = 80, message = "O assunto deve ter no máximo 80 caracteres.")
        String subject,
        @NotBlank(message = "O comunicado não pode ficar vazio.")
        @Size(max = 1000, message = "O comunicado deve ter no máximo 1.000 caracteres.")
        String body
) {
}
