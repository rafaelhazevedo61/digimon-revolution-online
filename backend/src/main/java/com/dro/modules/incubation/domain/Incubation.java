package com.dro.modules.incubation.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.shared.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Incubação.
 */
@Entity
@Table(name = "incubations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incubation {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

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

    public void markReadyIfFinished () {

        if (this.status != IncubationStatus.IN_PROGRESS) {
            throw new BadRequestException("Invalid incubation state");
        }

        if (this.finishAt.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Incubation not finished yet");
        }

        this.status = IncubationStatus.READY;
    }

    public void claim () {

        if (this.status != IncubationStatus.READY) {
            throw new BadRequestException("Incubation not ready");
        }

        this.status = IncubationStatus.CLAIMED;
    }

    public UUID getId () {
        return id;
    }

    public void setId (UUID id) {
        this.id = id;
    }

    public UUID getPlayerId () {
        return playerId;
    }

    public void setPlayerId (UUID playerId) {
        this.playerId = playerId;
    }

    public ItemType getDigitamaType () {
        return digitamaType;
    }

    public void setDigitamaType (ItemType digitamaType) {
        this.digitamaType = digitamaType;
    }

    public ItemType getIncubatorType () {
        return incubatorType;
    }

    public void setIncubatorType (ItemType incubatorType) {
        this.incubatorType = incubatorType;
    }

    public LocalDateTime getStartedAt () {
        return startedAt;
    }

    public void setStartedAt (LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishAt () {
        return finishAt;
    }

    public void setFinishAt (LocalDateTime finishAt) {
        this.finishAt = finishAt;
    }

    public IncubationStatus getStatus () {
        return status;
    }

    public void setStatus (IncubationStatus status) {
        this.status = status;
    }
}
