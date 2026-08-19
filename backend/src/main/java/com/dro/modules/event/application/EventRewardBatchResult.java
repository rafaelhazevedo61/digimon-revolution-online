package com.dro.modules.event.application;

import java.util.List;
import java.util.UUID;

/**
 * Resumo da criação em lote de premiações de eventos.
 *
 * @param createdCount quantidade de jogadores para os quais uma nova premiação foi criada
 * @param skippedCount quantidade de jogadores ignorados por idempotência
 * @param requestedCount quantidade de destinatários únicos solicitados
 * @param rewardIds identificadores das premiações criadas ou já existentes
 * @param skippedUsernames usernames que já possuíam a mesma origem e foram ignorados
 */
public record EventRewardBatchResult(
        int createdCount,
        int skippedCount,
        int requestedCount,
        List<UUID> rewardIds,
        List<String> skippedUsernames
) {
}
