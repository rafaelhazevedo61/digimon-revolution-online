package com.dro.modules.mail.api.dto.response;

/**
 * Resultado de uma ação processada em uma mensagem do Correio.
 *
 * @param completed indica se a operação foi concluída com sucesso; {@code false}
 *                  representa uma ação bloqueada ou uma mensagem indisponível
 * @param message mensagem localizada para exibição ao jogador
 */
public record MailActionResponse(
        boolean completed,
        String message
) {
}
