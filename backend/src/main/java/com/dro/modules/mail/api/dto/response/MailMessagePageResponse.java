package com.dro.modules.mail.api.dto.response;

import java.util.List;

/**
 * Página de resumos de mensagens retornada pela Entrada ou Enviadas.
 *
 * @param content mensagens da página atual
 * @param page índice da página, começando em zero
 * @param size tamanho efetivo da página após normalização
 * @param totalElements quantidade total de mensagens visíveis
 * @param totalPages quantidade total de páginas disponíveis
 */
public record MailMessagePageResponse(
        List<MailMessageSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
