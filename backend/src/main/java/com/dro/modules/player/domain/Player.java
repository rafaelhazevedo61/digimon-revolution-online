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
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private UserType userType = UserType.PLAYER;

    @Column(name = "token_version", nullable = false)
    @Builder.Default
    private int tokenVersion = 0;

    @Column(name = "max_digimon_slots", nullable = false)
    private int maxDigimonSlots = 3;

    @Column(name = "max_storage_slots", nullable = false)
    private int maxStorageSlots = 50;

    @Column(name = "arena_coins", nullable = false)
    @Builder.Default
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

    /** Informa se o jogador já concluiu a seleção do Digitama inicial. */
    public boolean hasSelectedStarter() {
        return starterSelected;
    }

    /** Marca a seleção inicial como concluída. */
    public void markStarterAsSelected() {
        this.starterSelected = true;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

    public UUID getId () {
        return id;
    }

    public String getUsername () {
        return username;
    }
}
