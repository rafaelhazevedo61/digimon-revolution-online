package com.dro.modules.event.domain;

import com.dro.modules.player.domain.Player;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Premiação persistente de um evento destinada a um jogador específico.
 *
 * <p>A premiação guarda o conteúdo que será entregue, sua origem idempotente,
 * o prazo de validade e o estado do resgate. A entrega é feita por meio de uma
 * mensagem {@code EVENT} do Correio, mas o estado da premiação é a fonte de
 * verdade para impedir duplicidade.</p>
 */
@Entity
@Table(name = "event_rewards", indexes = {
        @Index(name = "idx_event_reward_player_status", columnList = "player_id, status, expires_at"),
        @Index(name = "idx_event_reward_source", columnList = "source_type, source_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventReward {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "source_type", nullable = false, length = 64)
    private String sourceType;

    @Column(name = "source_id", nullable = false, length = 128)
    private String sourceId;

    @Column(nullable = false, length = 80)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(name = "bits_amount", nullable = false)
    @Builder.Default
    private int bitsAmount = 0;

    @Column(name = "item_type", length = 50)
    private String itemType;

    @Column(name = "item_quantity", nullable = false)
    @Builder.Default
    private int itemQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventRewardStatus status = EventRewardStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    /**
     * Verifica se a premiação pode ser resgatada no instante informado.
     *
     * <p>O resultado só é positivo quando o estado permanece {@code PENDING}
     * e o prazo ainda não terminou. O método não altera o estado; a transição
     * para {@code EXPIRED} é realizada pelo fluxo de processamento da ação.</p>
     *
     * @param now instante usado para validar a validade do prêmio
     * @return {@code true} quando o resgate ainda está disponível
     */
    public boolean isPendingAt(LocalDateTime now) {
        return status == EventRewardStatus.PENDING && expiresAt.isAfter(now);
    }
}
