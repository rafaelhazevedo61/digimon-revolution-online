package com.dro.modules.mail.domain;

/**
 * Categorias de mensagens que podem aparecer no Correio.
 *
 * <p>{@code PLAYER} representa comunicação entre jogadores. Os demais tipos
 * são mensagens geradas por sistemas do jogo ou por operações administrativas
 * e podem possuir regras e ações específicas.</p>
 */
public enum MailMessageType {
    /** Mensagem de texto enviada por um jogador a outro. */
    PLAYER,
    /** Mensagem genérica gerada pelo sistema. */
    SYSTEM,
    /** Notificação originada na Casa de Leilões. */
    AUCTION,
    /** Convite ou comunicado relacionado a clãs. */
    CLAN,
    /** Premiação ou comunicado relacionado a um evento. */
    EVENT,
    /** Comunicado criado por uma operação administrativa. */
    ADMIN
}
