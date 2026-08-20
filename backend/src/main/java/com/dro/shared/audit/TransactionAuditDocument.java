package com.dro.shared.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Documento de auditoria de uma transação de negócio concluída.
 *
 * <p>Este documento é histórico e observacional. O estado oficial da operação
 * continua pertencendo ao PostgreSQL.</p>
 *
 * @param id identificador interno do documento
 * @param eventId identificador idempotente do evento de auditoria
 * @param occurredAt instante UTC em que a operação ocorreu
 * @param correlationId identificador que conecta requisição e logs
 * @param actorId jogador, administrador ou sistema responsável
 * @param module módulo funcional que originou o evento
 * @param operation operação executada
 * @param entityType tipo da entidade afetada
 * @param entityId identificador da entidade afetada
 * @param eventType tipo estável do evento de negócio
 * @param result resultado da operação
 * @param summary resumo sanitizado para leitura humana
 * @param metadata dados adicionais sem segredos
 * @param durationMs duração da operação em milissegundos
 * @param schemaVersion versão do formato do documento
 */
@Document(collection = "dro_transaction_audits")
public record TransactionAuditDocument(
        @Id String id,
        @Indexed(unique = true) String eventId,
        @Indexed Instant occurredAt,
        @Indexed String correlationId,
        @Indexed String actorId,
        @Indexed String module,
        @Indexed String operation,
        String entityType,
        String entityId,
        @Indexed String eventType,
        AuditResult result,
        String summary,
        Map<String, Object> metadata,
        Long durationMs,
        Integer schemaVersion
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;
}
