package com.dro.shared.audit;

import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publica auditorias das operações administrativas com o administrador autenticado
 * como ator explícito.
 */
@Service
public class AdminAuditService {
    private final TransactionAuditPublisher transactionAuditPublisher;

    /**
     * Registra uma operação administrativa sem incluir segredos no metadata.
     *
     * @param authorization token JWT do administrador
     * @param eventType tipo estável do evento
     * @param aggregateType tipo do alvo da operação
     * @param aggregateId identificador do alvo ou da operação global
     * @param operation nome curto da operação
     * @param summary resumo sanitizado da operação
     * @param metadata parâmetros relevantes e não sensíveis
     */
    public void success(String authorization, String eventType, String aggregateType, String aggregateId, String operation, String summary, Map<String, Object> metadata) {
        UUID adminId = TokenExtractor.extractPlayerId(authorization);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("actorId", adminId.toString());
        payload.put("adminId", adminId.toString());
        payload.put("module", "admin");
        payload.put("operation", operation);
        payload.put("summary", summary);
        if (metadata != null) {
            payload.putAll(metadata);
        }
        transactionAuditPublisher.success("admin:" + eventType.toLowerCase() + ":" + aggregateId + ":" + UUID.randomUUID(), eventType, aggregateType, aggregateId, payload);
    }

    public AdminAuditService(final TransactionAuditPublisher transactionAuditPublisher) {
        this.transactionAuditPublisher = transactionAuditPublisher;
    }
}
