package com.dro.modules.event.domain;

import com.dro.modules.player.domain.Player;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Premiação persistente de um evento destinada a um jogador específico.
 *
 * <p>A premiação guarda o conteúdo que será entregue, sua origem idempotente,
 * o prazo de validade e o estado do resgate. Quando selecionada pelo catálogo,
 * a definição específica é preservada em {@code itemDefinitionCode}; o campo
 * {@code itemType} continua sendo mantido para compatibilidade com registros
 * legados. A entrega é feita por meio de uma mensagem {@code EVENT} do Correio,
 * mas o estado da premiação é a fonte de verdade para impedir duplicidade.</p>
 */
@Entity
@Table(name = "event_rewards", indexes = {@Index(name = "idx_event_reward_player_status", columnList = "player_id, status, expires_at"), @Index(name = "idx_event_reward_source", columnList = "source_type, source_id")})
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
    private int bitsAmount;
    @Column(name = "item_type", length = 50)
    private String itemType;
    @Column(name = "item_definition_code", length = 128)
    private String itemDefinitionCode;
    @Column(name = "item_quantity", nullable = false)
    private int itemQuantity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventRewardStatus status;
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

    private static int $default$bitsAmount() {
        return 0;
    }

    private static int $default$itemQuantity() {
        return 0;
    }

    private static EventRewardStatus $default$status() {
        return EventRewardStatus.PENDING;
    }


    public static class EventRewardBuilder {
        private UUID id;
        private Player player;
        private String sourceType;
        private String sourceId;
        private String subject;
        private String body;
        private boolean bitsAmount$set;
        private int bitsAmount$value;
        private String itemType;
        private String itemDefinitionCode;
        private boolean itemQuantity$set;
        private int itemQuantity$value;
        private boolean status$set;
        private EventRewardStatus status$value;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime claimedAt;

        EventRewardBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder player(final Player player) {
            this.player = player;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder sourceType(final String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder sourceId(final String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder body(final String body) {
            this.body = body;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder bitsAmount(final int bitsAmount) {
            this.bitsAmount$value = bitsAmount;
            bitsAmount$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder itemType(final String itemType) {
            this.itemType = itemType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder itemDefinitionCode(final String itemDefinitionCode) {
            this.itemDefinitionCode = itemDefinitionCode;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder itemQuantity(final int itemQuantity) {
            this.itemQuantity$value = itemQuantity;
            itemQuantity$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder status(final EventRewardStatus status) {
            this.status$value = status;
            status$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder expiresAt(final LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EventReward.EventRewardBuilder claimedAt(final LocalDateTime claimedAt) {
            this.claimedAt = claimedAt;
            return this;
        }

        public EventReward build() {
            int bitsAmount$value = this.bitsAmount$value;
            if (!this.bitsAmount$set) bitsAmount$value = EventReward.$default$bitsAmount();
            int itemQuantity$value = this.itemQuantity$value;
            if (!this.itemQuantity$set) itemQuantity$value = EventReward.$default$itemQuantity();
            EventRewardStatus status$value = this.status$value;
            if (!this.status$set) status$value = EventReward.$default$status();
            return new EventReward(this.id, this.player, this.sourceType, this.sourceId, this.subject, this.body, bitsAmount$value, this.itemType, this.itemDefinitionCode, itemQuantity$value, status$value, this.createdAt, this.expiresAt, this.claimedAt);
        }

        @Override
        public String toString() {
            return "EventReward.EventRewardBuilder(id=" + this.id + ", player=" + this.player + ", sourceType=" + this.sourceType + ", sourceId=" + this.sourceId + ", subject=" + this.subject + ", body=" + this.body + ", bitsAmount$value=" + this.bitsAmount$value + ", itemType=" + this.itemType + ", itemDefinitionCode=" + this.itemDefinitionCode + ", itemQuantity$value=" + this.itemQuantity$value + ", status$value=" + this.status$value + ", createdAt=" + this.createdAt + ", expiresAt=" + this.expiresAt + ", claimedAt=" + this.claimedAt + ")";
        }
    }

    public static EventReward.EventRewardBuilder builder() {
        return new EventReward.EventRewardBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getSourceType() {
        return this.sourceType;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getBody() {
        return this.body;
    }

    public int getBitsAmount() {
        return this.bitsAmount;
    }

    public String getItemType() {
        return this.itemType;
    }

    public String getItemDefinitionCode() {
        return this.itemDefinitionCode;
    }

    public int getItemQuantity() {
        return this.itemQuantity;
    }

    public EventRewardStatus getStatus() {
        return this.status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public LocalDateTime getClaimedAt() {
        return this.claimedAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public void setSourceType(final String sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceId(final String sourceId) {
        this.sourceId = sourceId;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public void setBitsAmount(final int bitsAmount) {
        this.bitsAmount = bitsAmount;
    }

    public void setItemType(final String itemType) {
        this.itemType = itemType;
    }

    public void setItemDefinitionCode(final String itemDefinitionCode) {
        this.itemDefinitionCode = itemDefinitionCode;
    }

    public void setItemQuantity(final int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public void setStatus(final EventRewardStatus status) {
        this.status = status;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(final LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setClaimedAt(final LocalDateTime claimedAt) {
        this.claimedAt = claimedAt;
    }

    public EventReward() {
        this.bitsAmount = EventReward.$default$bitsAmount();
        this.itemQuantity = EventReward.$default$itemQuantity();
        this.status = EventReward.$default$status();
    }

    public EventReward(final UUID id, final Player player, final String sourceType, final String sourceId, final String subject, final String body, final int bitsAmount, final String itemType, final String itemDefinitionCode, final int itemQuantity, final EventRewardStatus status, final LocalDateTime createdAt, final LocalDateTime expiresAt, final LocalDateTime claimedAt) {
        this.id = id;
        this.player = player;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.subject = subject;
        this.body = body;
        this.bitsAmount = bitsAmount;
        this.itemType = itemType;
        this.itemDefinitionCode = itemDefinitionCode;
        this.itemQuantity = itemQuantity;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.claimedAt = claimedAt;
    }
}
