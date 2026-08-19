package com.dro.modules.mail.domain;

/**
 * Centraliza os limites e as regras quantitativas compartilhadas pelo Correio.
 *
 * <p>As constantes desta classe são usadas tanto na validação do backend quanto
 * na definição dos limites esperados pelos clientes. Alterá-las pode afetar
 * mensagens já existentes, paginação e proteção contra spam.</p>
 */
public final class MailRules {

    /** Tamanho máximo do assunto de uma mensagem comum ou do sistema. */
    public static final int SUBJECT_MAX_LENGTH = 80;
    /** Tamanho máximo do corpo de uma mensagem. */
    public static final int BODY_MAX_LENGTH = 1000;

    /** Maior quantidade de mensagens retornada em uma página. */
    public static final int MAX_PAGE_SIZE = 50;

    /** Maior quantidade de mensagens comuns que um jogador pode enviar por minuto. */
    public static final int MAX_MESSAGES_PER_MINUTE = 10;

    /** Tamanho máximo do tipo de origem de uma mensagem do sistema. */
    public static final int SOURCE_TYPE_MAX_LENGTH = 64;

    /** Tamanho máximo do tipo de ação disponível em uma mensagem. */
    public static final int ACTION_TYPE_MAX_LENGTH = 64;

    /** Tamanho máximo da chave que impede a entrega duplicada de uma mensagem do sistema. */
    public static final int DELIVERY_KEY_MAX_LENGTH = 128;

    private MailRules() {
    }
}
