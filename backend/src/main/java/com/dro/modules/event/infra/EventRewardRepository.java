package com.dro.modules.event.infra;

import com.dro.modules.event.domain.EventReward;
import com.dro.modules.event.domain.EventRewardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso persistente às premiações de eventos.
 *
 * <p>As operações especiais deste repositório preservam duas garantias: o
 * jogador só pode resgatar a própria premiação e o banco impede uma segunda
 * premiação para a mesma combinação de origem e jogador.</p>
 */
public interface EventRewardRepository extends JpaRepository<EventReward, UUID> {

    /**
     * Busca e bloqueia a premiação do jogador durante o resgate.
     *
     * <p>O lock pessimista serializa tentativas concorrentes de resgate para o
     * mesmo registro e evita que duas transações entreguem o mesmo prêmio.</p>
     *
     * @param id identificador da premiação
     * @param playerId jogador destinatário autenticado
     * @return premiação pertencente ao jogador, quando encontrada
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM EventReward r WHERE r.id = :id AND r.player.id = :playerId")
    Optional<EventReward> findByIdAndPlayerIdForUpdate(
            @Param("id") UUID id,
            @Param("playerId") UUID playerId
    );

    /**
     * Localiza a premiação existente para uma origem e jogador.
     *
     * @param sourceType tipo da origem do evento
     * @param sourceId identificador estável da origem
     * @param playerId jogador destinatário
     * @return premiação já criada para a combinação informada, quando existir
     */
    Optional<EventReward> findBySourceTypeAndSourceIdAndPlayerId(
            String sourceType,
            String sourceId,
            UUID playerId
    );

    /**
     * Insere a premiação somente quando a combinação origem-jogador ainda não existe.
     *
     * <p>O {@code ON CONFLICT DO NOTHING} torna o lote seguro para reprocessamento
     * e permite informar ao chamador quais destinatários foram ignorados.</p>
     *
     * @return {@code 1} quando uma linha foi criada; {@code 0} quando a chave
     *         única já estava ocupada
     */
    @Modifying
    @Query(value = """
            INSERT INTO event_rewards (
                id, player_id, source_type, source_id, subject, body,
                bits_amount, item_type, item_definition_code, item_quantity, status,
                created_at, expires_at
            ) VALUES (
                :id, :playerId, :sourceType, :sourceId, :subject, :body,
                :bitsAmount, :itemType, :itemDefinitionCode, :itemQuantity, :status,
                :createdAt, :expiresAt
            ) ON CONFLICT (source_type, source_id, player_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("playerId") UUID playerId,
            @Param("sourceType") String sourceType,
            @Param("sourceId") String sourceId,
            @Param("subject") String subject,
            @Param("body") String body,
            @Param("bitsAmount") int bitsAmount,
            @Param("itemType") String itemType,
            @Param("itemDefinitionCode") String itemDefinitionCode,
            @Param("itemQuantity") int itemQuantity,
            @Param("status") String status,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("expiresAt") LocalDateTime expiresAt
    );
}
