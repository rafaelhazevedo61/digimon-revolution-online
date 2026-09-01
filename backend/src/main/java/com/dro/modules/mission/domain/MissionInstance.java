package com.dro.modules.mission.domain;

import com.dro.modules.mission.domain.MissionStatus;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mission_instances")
public class MissionInstance {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "slot_number", nullable = false)
    private int slotNumber;

    @Column(name = "auto_repeat_enabled", nullable = false)
    private boolean autoRepeatEnabled;

    @Column(name = "auto_claim_enabled", nullable = false)
    private boolean autoClaimEnabled;

    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;

    @Column(name = "digimon_2_id")
    private UUID digimon2Id;

    @Column(name = "digimon_3_id")
    private UUID digimon3Id;

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
        this.slotNumber = 1;
    }

    public MissionInstance(UUID playerId, UUID digimonId, String missionId, Duration duration) {
        this(playerId, null, 1, List.of(digimonId), missionId, duration);
    }

    public MissionInstance(UUID playerId, UUID teamId, List<UUID> digimonIds, String missionId, Duration duration) {
        this(playerId, teamId, 1, digimonIds, missionId, duration);
    }

    public MissionInstance(UUID playerId, UUID teamId, int slotNumber, List<UUID> digimonIds, String missionId, Duration duration) {
        if (slotNumber < 1 || slotNumber > 3) {
            throw new IllegalArgumentException("Mission slot must be between 1 and 3");
        }
        if (digimonIds == null || digimonIds.isEmpty() || digimonIds.size() > 3) {
            throw new IllegalArgumentException("A mission instance must have between one and three Digimons");
        }
        this.playerId = playerId;
        this.teamId = teamId;
        this.slotNumber = slotNumber;
        this.digimonId = digimonIds.get(0);
        this.digimon2Id = digimonIds.size() > 1 ? digimonIds.get(1) : null;
        this.digimon3Id = digimonIds.size() > 2 ? digimonIds.get(2) : null;
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
                !Instant.now().isBefore(endsAt)) {
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

    public UUID getTeamId() {
        return teamId;
    }

    public int getSlotNumber() {
        return slotNumber;
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

    public UUID getDigimonId () {
        return digimonId;
    }

    public UUID getDigimon2Id() {
        return digimon2Id;
    }

    public UUID getDigimon3Id() {
        return digimon3Id;
    }

    public List<UUID> getDigimonIds() {
        if (digimon2Id == null) return List.of(digimonId);
        if (digimon3Id == null) return List.of(digimonId, digimon2Id);
        return List.of(digimonId, digimon2Id, digimon3Id);
    }

    public String getMissionId() {
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