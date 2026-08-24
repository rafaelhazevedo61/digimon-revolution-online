package com.dro.modules.player.domain;

import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.digitama.domain.enums.DigitamaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conta persistente do jogador e raiz de acesso aos recursos do jogo.
 *
 * <p>O jogador mantém o Digimon ativo, o vínculo opcional com clã, limites de
 * armazenamento e o tipo de usuário usado na autorização administrativa. Bits,
 * inventário e equipamentos ficam vinculados aos Digimons associados.</p>
 */
@Entity
@Table(name = "players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Player {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private DigitamaType selectedDigitama;

    @Column(name = "active_digimon_id")
    private UUID activeDigimonId;

    @Column(name = "last_mission_at")
    private LocalDateTime lastMissionAt;

    @Column(name = "has_selected_starter", nullable = false)
    private boolean starterSelected;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType = UserType.PLAYER;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @Column(name = "max_digimon_slots", nullable = false)
    private int maxDigimonSlots = 3;

    @Column(name = "max_storage_slots", nullable = false)
    private int maxStorageSlots = 50;

    @Column(name = "arena_coins", nullable = false)
    private int arenaCoins = 0;

    @Column(name = "clan_id")
    private UUID clanId;

    @Column(name = "clan_role")
    @Enumerated(EnumType.STRING)
    private ClanRole clanRole;

    @Column(name = "clan_joined_at")
    private LocalDateTime clanJoinedAt;

    @Column(name = "arena_daily_reset_at")
    private LocalDateTime arenaDailyResetAt;

    public static Player createPlayer (
            UUID id,
            String username,
            String email,
            String password,
            LocalDateTime createdAt
    ) {
        Player player = new Player();

        player.id = id;
        player.username = username;
        player.email = email;
        player.password = password;
        player.createdAt = createdAt;
        player.maxDigimonSlots = 3;
        player.maxStorageSlots = 50;
        player.userType = UserType.PLAYER;

        return player;
    }

    /**
     * Informa se o jogador já concluiu a seleção do Digitama inicial.
     */
    public boolean hasSelectedStarter () {
        return starterSelected;
    }

    /**
     * Marca a seleção inicial como concluída.
     */
    public void markStarterAsSelected () {
        this.starterSelected = true;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public void incrementTokenVersion () {
        this.tokenVersion++;
    }

    public UUID getId () {
        return id;
    }

    public String getUsername () {
        return username;
    }

    public void setId (UUID id) {
        this.id = id;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public String getPassword () {
        return password;
    }

    public LocalDateTime getCreatedAt () {
        return createdAt;
    }

    public void setCreatedAt (LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DigitamaType getSelectedDigitama () {
        return selectedDigitama;
    }

    public void setSelectedDigitama (DigitamaType selectedDigitama) {
        this.selectedDigitama = selectedDigitama;
    }

    public UUID getActiveDigimonId () {
        return activeDigimonId;
    }

    public void setActiveDigimonId (UUID activeDigimonId) {
        this.activeDigimonId = activeDigimonId;
    }

    public LocalDateTime getLastMissionAt () {
        return lastMissionAt;
    }

    public void setLastMissionAt (LocalDateTime lastMissionAt) {
        this.lastMissionAt = lastMissionAt;
    }

    public boolean isStarterSelected () {
        return starterSelected;
    }

    public void setStarterSelected (boolean starterSelected) {
        this.starterSelected = starterSelected;
    }

    public UserType getUserType () {
        return userType;
    }

    public void setUserType (UserType userType) {
        this.userType = userType;
    }

    public int getTokenVersion () {
        return tokenVersion;
    }

    public void setTokenVersion (int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public int getMaxDigimonSlots () {
        return maxDigimonSlots;
    }

    public void setMaxDigimonSlots (int maxDigimonSlots) {
        this.maxDigimonSlots = maxDigimonSlots;
    }

    public int getMaxStorageSlots () {
        return maxStorageSlots;
    }

    public void setMaxStorageSlots (int maxStorageSlots) {
        this.maxStorageSlots = maxStorageSlots;
    }

    public int getArenaCoins () {
        return arenaCoins;
    }

    public void setArenaCoins (int arenaCoins) {
        this.arenaCoins = arenaCoins;
    }

    public UUID getClanId () {
        return clanId;
    }

    public void setClanId (UUID clanId) {
        this.clanId = clanId;
    }

    public ClanRole getClanRole () {
        return clanRole;
    }

    public void setClanRole (ClanRole clanRole) {
        this.clanRole = clanRole;
    }

    public LocalDateTime getClanJoinedAt () {
        return clanJoinedAt;
    }

    public void setClanJoinedAt (LocalDateTime clanJoinedAt) {
        this.clanJoinedAt = clanJoinedAt;
    }

    public LocalDateTime getArenaDailyResetAt () {
        return arenaDailyResetAt;
    }

    public void setArenaDailyResetAt (LocalDateTime arenaDailyResetAt) {
        this.arenaDailyResetAt = arenaDailyResetAt;
    }
}
