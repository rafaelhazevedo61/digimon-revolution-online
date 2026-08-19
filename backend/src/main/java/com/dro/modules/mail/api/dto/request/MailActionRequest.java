package com.dro.modules.mail.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MailActionRequest(
        @NotBlank(message = "Informe a ação da mensagem.")
        String action
) {
}
