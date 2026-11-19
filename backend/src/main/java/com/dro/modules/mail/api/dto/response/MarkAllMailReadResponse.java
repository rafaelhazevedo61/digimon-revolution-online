package com.dro.modules.mail.api.dto.response;

/**
 * Resultado da marcação em lote das mensagens elegíveis do Correio.
 *
 * @param markedCount quantidade de mensagens comuns que passaram a ser lidas
 */
public record MarkAllMailReadResponse(
        int markedCount
) {
}
