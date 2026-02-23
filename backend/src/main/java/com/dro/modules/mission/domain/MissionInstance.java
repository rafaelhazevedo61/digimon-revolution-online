package com.dro.modules.mission.domain;

import com.dro.modules.mission.domain.MissionStatus;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mission_instances")
public class MissionInstance {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;

    @Column(name = "mission_id", nullable = false)
    private String missionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    protected MissionInstance () {
        // JPA
    }

    public MissionInstance (UUID playerId,
                            UUID digimonId,
                            String missionId,
                            Duration duration) {

        this.playerId = playerId;
        this.digimonId = digimonId;
        this.missionId = missionId;

        this.status = MissionStatus.RUNNING;

        this.startedAt = Instant.now();
        this.endsAt = startedAt.plus(duration);
    }

    // ===============================
    // State transitions
    // ===============================

    /**
     * Atualiza o status para COMPLETED caso o tempo tenha terminado.
     */
    public boolean updateStatusIfFinished() {
        if (status == MissionStatus.RUNNING &&
                Instant.now().isAfter(endsAt)) {
            this.status = MissionStatus.COMPLETED;
            return true;
        }
        return false;
    }

    /**
     * Marca a missão como CLAIMED.
     * Só pode ser chamada se estiver COMPLETED.
     */
    public void markClaimed () {

        if (status != MissionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Missão não pode ser resgatada no estado atual: " + status
            );
        }

        this.status = MissionStatus.CLAIMED;
        this.claimedAt = Instant.now();
    }

    // ===============================
    // Helpers
    // ===============================

    public boolean isRunning () {
        return status == MissionStatus.RUNNING;
    }

    public boolean isCompleted () {
        return status == MissionStatus.COMPLETED;
    }

    public boolean isClaimed () {
        return status == MissionStatus.CLAIMED;
    }

    public boolean isReadyToClaim () {
        return status == MissionStatus.COMPLETED;
    }

    public boolean canBeClaimed() {
        return status == MissionStatus.COMPLETED;
    }

    public boolean isAlreadyClaimed() {
        return status == MissionStatus.CLAIMED;
    }

    // ===============================
    // Getters
    // ===============================

    public UUID getId () {
        return id;
    }

    public UUID getPlayerId () {
        return playerId;
    }

    public UUID getDigimonId () {
        return digimonId;
    }

    public String getMissionId () {
        return missionId;
    }

    public MissionStatus getStatus () {
        return status;
    }

    public Instant getStartedAt () {
        return startedAt;
    }

    public Instant getEndsAt () {
        return endsAt;
    }

    public Instant getClaimedAt () {
        return claimedAt;
    }
}