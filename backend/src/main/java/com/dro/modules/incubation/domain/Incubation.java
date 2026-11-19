package com.dro.modules.incubation.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.shared.exception.BadRequestException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Incubação.
 */
@Entity
@Table(name = "incubations")
public class Incubation {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID playerId;
    @Column(name = "slot_number", nullable = false)
    private int slotNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType digitamaType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType incubatorType;
    @Column(nullable = false)
    private LocalDateTime startedAt;
    @Column(nullable = false)
    private LocalDateTime finishAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncubationStatus status;
    @Column(name = "auto_repeat_enabled", nullable = false)
    private boolean autoRepeatEnabled;
    @Column(name = "auto_claim_enabled", nullable = false)
    private boolean autoClaimEnabled;
    @Column(name = "automation_pause_reason")
    private String automationPauseReason;
    @Column(name = "automation_paused_at")
    private LocalDateTime automationPausedAt;
    @Column(name = "automation_last_error_code")
    private String automationLastErrorCode;

    public void markReadyIfFinished() {
        if (this.status != IncubationStatus.IN_PROGRESS) {
            throw new BadRequestException("Invalid incubation state");
        }
        if (this.finishAt.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Incubation not finished yet");
        }
        this.status = IncubationStatus.READY;
    }

    public void claim() {
        if (this.status != IncubationStatus.READY) {
            throw new BadRequestException("Incubation not ready");
        }
        this.status = IncubationStatus.CLAIMED;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }
    public int getSlotNumber() {
        return slotNumber;
    }
    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }
    public ItemType getDigitamaType() {
        return digitamaType;
    }

    public void setDigitamaType(ItemType digitamaType) {
        this.digitamaType = digitamaType;
    }

    public ItemType getIncubatorType() {
        return incubatorType;
    }

    public void setIncubatorType(ItemType incubatorType) {
        this.incubatorType = incubatorType;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishAt() {
        return finishAt;
    }

    public void setFinishAt(LocalDateTime finishAt) {
        this.finishAt = finishAt;
    }

    public IncubationStatus getStatus() {
        return status;
    }

    public void setStatus(IncubationStatus status) {
        this.status = status;
    }

    public boolean isAutoRepeatEnabled() {
        return autoRepeatEnabled;
    }

    public void setAutoRepeatEnabled(boolean autoRepeatEnabled) {
        this.autoRepeatEnabled = autoRepeatEnabled;
    }

    public boolean isAutoClaimEnabled() {
        return autoClaimEnabled;
    }

    public void setAutoClaimEnabled(boolean autoClaimEnabled) {
        this.autoClaimEnabled = autoClaimEnabled;
    }

    public String getAutomationPauseReason() { return automationPauseReason; }
    public LocalDateTime getAutomationPausedAt() { return automationPausedAt; }
    public String getAutomationLastErrorCode() { return automationLastErrorCode; }

    public void pauseAutomation(String reason, String errorCode) {
        this.autoClaimEnabled = false;
        this.autoRepeatEnabled = false;
        this.automationPauseReason = reason;
        this.automationLastErrorCode = errorCode;
        this.automationPausedAt = LocalDateTime.now();
    }

    public void clearAutomationPause() {
        this.automationPauseReason = null;
        this.automationLastErrorCode = null;
        this.automationPausedAt = null;
    }


    private static int $default$slotNumber() {
        return 1;
    }

    public static class IncubationBuilder {
        private UUID id;
        private UUID playerId;
        private boolean slotNumber$set;
        private int slotNumber$value;
        private ItemType digitamaType;
        private ItemType incubatorType;
        private LocalDateTime startedAt;
        private LocalDateTime finishAt;
        private IncubationStatus status;
        private boolean autoRepeatEnabled;
        private boolean autoClaimEnabled;

        IncubationBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Incubation.IncubationBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Incubation.IncubationBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }
        /**
         * @return {@code this}.
         */
        public Incubation.IncubationBuilder slotNumber(final int slotNumber) {
            this.slotNumber$value = slotNumber;
            slotNumber$set = true;
            return this;
        }
        /**
         * @return {@code this}.
         */
        public Incubation.IncubationBuilder digitamaType(final ItemType digitamaType) {
            this.digitamaType = digitamaType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Incubation.IncubationBuilder incubatorType(final ItemType incubatorType) {
            this.incubatorType = incubatorType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Incubation.IncubationBuilder startedAt(final LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Incubation.IncubationBuilder finishAt(final LocalDateTime finishAt) {
            this.finishAt = finishAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Incubation.IncubationBuilder status(final IncubationStatus status) {
            this.status = status;
            return this;
        }

        public Incubation.IncubationBuilder autoRepeatEnabled(final boolean autoRepeatEnabled) {
            this.autoRepeatEnabled = autoRepeatEnabled;
            return this;
        }

        public Incubation.IncubationBuilder autoClaimEnabled(final boolean autoClaimEnabled) {
            this.autoClaimEnabled = autoClaimEnabled;
            return this;
        }

        public Incubation build() {
            int slotNumber$value = this.slotNumber$value;
            if (!this.slotNumber$set) slotNumber$value = Incubation.$default$slotNumber();
            return new Incubation(this.id, this.playerId, slotNumber$value, this.digitamaType, this.incubatorType, this.startedAt, this.finishAt, this.status, this.autoRepeatEnabled, this.autoClaimEnabled);
        }
        @Override
        public String toString() {
            return "Incubation.IncubationBuilder(id=" + this.id + ", playerId=" + this.playerId + ", slotNumber=" + this.slotNumber$value + ", digitamaType=" + this.digitamaType + ", incubatorType=" + this.incubatorType + ", startedAt=" + this.startedAt + ", finishAt=" + this.finishAt + ", status=" + this.status + ", autoRepeatEnabled=" + this.autoRepeatEnabled + ", autoClaimEnabled=" + this.autoClaimEnabled + ")";
        }
    }

    public static Incubation.IncubationBuilder builder() {
        return new Incubation.IncubationBuilder();
    }

    public Incubation() {
        this.slotNumber = Incubation.$default$slotNumber();
    }
    public Incubation(final UUID id, final UUID playerId, final int slotNumber, final ItemType digitamaType, final ItemType incubatorType, final LocalDateTime startedAt, final LocalDateTime finishAt, final IncubationStatus status, final boolean autoRepeatEnabled, final boolean autoClaimEnabled) {
        this.id = id;
        this.playerId = playerId;
        this.slotNumber = slotNumber;
        this.digitamaType = digitamaType;
        this.incubatorType = incubatorType;
        this.startedAt = startedAt;
        this.finishAt = finishAt;
        this.status = status;
        this.autoRepeatEnabled = autoRepeatEnabled;
        this.autoClaimEnabled = autoClaimEnabled;
    }
}
