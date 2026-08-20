package com.dro.shared.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Documento persistente de erro técnico ou rejeição de negócio.
 *
 * <p>Mensagens, stack traces e metadados devem ser sanitizados antes da criação
 * deste documento. Tokens, senhas e payloads sensíveis não devem ser persistidos.</p>
 *
 * @param id identificador interno do documento
 * @param errorId identificador estável do erro
 * @param occurredAt instante UTC do erro
 * @param correlationId identificador da requisição relacionada
 * @param actorId jogador, administrador ou sistema identificado
 * @param module módulo funcional em que ocorreu a falha
 * @param operation operação que estava em execução
 * @param httpMethod método HTTP, quando aplicável
 * @param path rota sem query string sensível
 * @param httpStatus status HTTP retornado
 * @param errorCode código estável para pesquisa e métricas
 * @param exceptionType tipo da exceção
 * @param message mensagem sanitizada
 * @param stackTrace stack trace limitado e sanitizado
 * @param transactionOutcome desfecho conhecido da transação
 * @param metadata contexto adicional seguro
 * @param retryCount quantidade de tentativas conhecidas
 * @param severity severidade do erro
 * @param schemaVersion versão do formato do documento
 */
@Document(collection = "dro_error_logs")
public record ErrorLogDocument(
        @Id String id,
        @Indexed(unique = true) String errorId,
        @Indexed Instant occurredAt,
        @Indexed String correlationId,
        @Indexed String actorId,
        @Indexed String module,
        @Indexed String operation,
        String httpMethod,
        String path,
        @Indexed Integer httpStatus,
        @Indexed String errorCode,
        String exceptionType,
        String message,
        String stackTrace,
        @Indexed TransactionOutcome transactionOutcome,
        Map<String, Object> metadata,
        Integer retryCount,
        @Indexed AuditSeverity severity,
        Integer schemaVersion
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;
}
