package com.dro.modules.mail.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Ação solicitada para uma mensagem especial do Correio.
 *
 * <p>O valor é revalidado pelo backend de acordo com o tipo da mensagem. Entre
 * as ações existentes estão {@code CLAIM} para premiações e {@code ACCEPT} ou
 * {@code DECLINE} para convites de clã.</p>
 *
 * @param action nome da ação solicitada
 */
public record MailActionRequest(
        @NotBlank(message = "Informe a ação da mensagem.")
        String action
) {
}
