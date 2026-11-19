package com.dro.modules.digitama.domain;

import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.digitama.domain.enums.HatchSource;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Digitama.
 */
@Entity
@Table(name = "digitama_history")
public class DigitamaHistory {
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @Enumerated(EnumType.STRING)
    @Column(name = "digitama_type", nullable = false)
    private DigitamaType digitamaType;
    @Column(name = "digimon_name", nullable = false)
    private String digimonName;
    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;
    @Column(name = "hatched_at", nullable = false)
    private LocalDateTime hatchedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HatchSource source;


    public static class DigitamaHistoryBuilder {
        private UUID id;
        private UUID playerId;
        private DigitamaType digitamaType;
        private String digimonName;
        private UUID digimonId;
        private LocalDateTime hatchedAt;
        private HatchSource source;

        DigitamaHistoryBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public DigitamaHistory.DigitamaHistoryBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaHistory.DigitamaHistoryBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaHistory.DigitamaHistoryBuilder digitamaType(final DigitamaType digitamaType) {
            this.digitamaType = digitamaType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaHistory.DigitamaHistoryBuilder digimonName(final String digimonName) {
            this.digimonName = digimonName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaHistory.DigitamaHistoryBuilder digimonId(final UUID digimonId) {
            this.digimonId = digimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaHistory.DigitamaHistoryBuilder hatchedAt(final LocalDateTime hatchedAt) {
            this.hatchedAt = hatchedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaHistory.DigitamaHistoryBuilder source(final HatchSource source) {
            this.source = source;
            return this;
        }

        public DigitamaHistory build() {
            return new DigitamaHistory(this.id, this.playerId, this.digitamaType, this.digimonName, this.digimonId, this.hatchedAt, this.source);
        }

        @Override
        public String toString() {
            return "DigitamaHistory.DigitamaHistoryBuilder(id=" + this.id + ", playerId=" + this.playerId + ", digitamaType=" + this.digitamaType + ", digimonName=" + this.digimonName + ", digimonId=" + this.digimonId + ", hatchedAt=" + this.hatchedAt + ", source=" + this.source + ")";
        }
    }

    public static DigitamaHistory.DigitamaHistoryBuilder builder() {
        return new DigitamaHistory.DigitamaHistoryBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public DigitamaType getDigitamaType() {
        return this.digitamaType;
    }

    public String getDigimonName() {
        return this.digimonName;
    }

    public UUID getDigimonId() {
        return this.digimonId;
    }

    public LocalDateTime getHatchedAt() {
        return this.hatchedAt;
    }

    public HatchSource getSource() {
        return this.source;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public void setDigitamaType(final DigitamaType digitamaType) {
        this.digitamaType = digitamaType;
    }

    public void setDigimonName(final String digimonName) {
        this.digimonName = digimonName;
    }

    public void setDigimonId(final UUID digimonId) {
        this.digimonId = digimonId;
    }

    public void setHatchedAt(final LocalDateTime hatchedAt) {
        this.hatchedAt = hatchedAt;
    }

    public void setSource(final HatchSource source) {
        this.source = source;
    }

    public DigitamaHistory() {
    }

    public DigitamaHistory(final UUID id, final UUID playerId, final DigitamaType digitamaType, final String digimonName, final UUID digimonId, final LocalDateTime hatchedAt, final HatchSource source) {
        this.id = id;
        this.playerId = playerId;
        this.digitamaType = digitamaType;
        this.digimonName = digimonName;
        this.digimonId = digimonId;
        this.hatchedAt = hatchedAt;
        this.source = source;
    }
}
