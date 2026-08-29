package com.dro.modules.player.domain;

import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.digitama.domain.enums.DigitamaType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conta persistente do jogador e raiz de acesso aos recursos do jogo.
 *
 * <p>O jogador mantém o Digimon ativo, o vínculo opcional com clã, limites de
 * armazenamento e o tipo de usuário usado na autorização administrativa. Bits,
 * inventário e equipamentos ficam vinculados ao jogador.</p>
 */
@Entity
@Table(name = "players")
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
    private UserType userType;
    @Column(name = "token_version", nullable = false)
    private int tokenVersion;
    @Column(name = "max_digimon_slots", nullable = false)
    private int maxDigimonSlots;
    @Column(name = "max_storage_slots", nullable = false)
    private int maxStorageSlots;
    @Column(name = "arena_coins", nullable = false)
    private int arenaCoins;
    @Column(name = "bits", nullable = false)
    private int bits;
    @Column(name = "digital_data", nullable = false)
    private int digitalData;
    @Column(name = "username_change_count", nullable = false)
    private int usernameChangeCount;
    @Column(name = "unlocked_incubation_slots", nullable = false)
    private int unlockedIncubationSlots;
    @Column(name = "clan_id")
    private UUID clanId;
    @Column(name = "clan_role")
    @Enumerated(EnumType.STRING)
    private ClanRole clanRole;
    @Column(name = "clan_joined_at")
    private LocalDateTime clanJoinedAt;
    @Column(name = "arena_daily_reset_at")
    private LocalDateTime arenaDailyResetAt;

    public static Player createPlayer(UUID id, String username, String email, String password, LocalDateTime createdAt) {
        Player player = new Player();
        player.id = id;
        player.username = username;
        player.email = email;
        player.password = password;
        player.createdAt = createdAt;
        player.maxDigimonSlots = 1;
        player.maxStorageSlots = 50;
        player.unlockedIncubationSlots = 1;
        player.userType = UserType.PLAYER;
        return player;
    }

    /**
     * Informa se o jogador já concluiu a seleção do Digitama inicial.
     */
    public boolean hasSelectedStarter() {
        return starterSelected;
    }

    /**
     * Marca a seleção inicial como concluída.
     */
    public void markStarterAsSelected() {
        this.starterSelected = true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUsernameChangeCount() {
        return usernameChangeCount;
    }

    public void incrementUsernameChangeCount() {
        this.usernameChangeCount++;
    }

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DigitamaType getSelectedDigitama() {
        return selectedDigitama;
    }

    public void setSelectedDigitama(DigitamaType selectedDigitama) {
        this.selectedDigitama = selectedDigitama;
    }

    public UUID getActiveDigimonId() {
        return activeDigimonId;
    }

    public void setActiveDigimonId(UUID activeDigimonId) {
        this.activeDigimonId = activeDigimonId;
    }

    public LocalDateTime getLastMissionAt() {
        return lastMissionAt;
    }

    public void setLastMissionAt(LocalDateTime lastMissionAt) {
        this.lastMissionAt = lastMissionAt;
    }

    public boolean isStarterSelected() {
        return starterSelected;
    }

    public void setStarterSelected(boolean starterSelected) {
        this.starterSelected = starterSelected;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public int getMaxDigimonSlots() {
        return maxDigimonSlots;
    }

    public void setMaxDigimonSlots(int maxDigimonSlots) {
        this.maxDigimonSlots = maxDigimonSlots;
    }

    public int getMaxStorageSlots() {
        return maxStorageSlots;
    }

    public void setMaxStorageSlots(int maxStorageSlots) {
        this.maxStorageSlots = maxStorageSlots;
    }

    public int getArenaCoins() {
        return arenaCoins;
    }

    public void setArenaCoins(int arenaCoins) {
        this.arenaCoins = arenaCoins;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        if (bits < 0) throw new IllegalArgumentException("Bits cannot be negative");
        this.bits = bits;
    }

    public void addBits(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Bits amount cannot be negative");
        if ((long) bits + amount > Integer.MAX_VALUE) throw new IllegalArgumentException("Bits overflow");
        this.bits += amount;
    }

    public boolean spendBits(int amount) {
        if (amount < 0 || bits < amount) return false;
        bits -= amount;
        return true;
    }

    public int getUnlockedIncubationSlots() {
        return unlockedIncubationSlots;
    }

    public void setUnlockedIncubationSlots(int unlockedIncubationSlots) {
        this.unlockedIncubationSlots = unlockedIncubationSlots;
    }

    public int getDigitalData() {
        return digitalData;
    }

    public void setDigitalData(int digitalData) {
        if (digitalData < 0) throw new IllegalArgumentException("Digital Data cannot be negative");
        this.digitalData = digitalData;
    }

    public void addDigitalData(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Digital Data amount cannot be negative");
        this.digitalData += amount;
    }

    public boolean spendDigitalData(int amount) {
        if (amount < 0 || digitalData < amount) return false;
        digitalData -= amount;
        return true;
    }

    public UUID getClanId() {
        return clanId;
    }

    public void setClanId(UUID clanId) {
        this.clanId = clanId;
    }

    public ClanRole getClanRole() {
        return clanRole;
    }

    public void setClanRole(ClanRole clanRole) {
        this.clanRole = clanRole;
    }

    public LocalDateTime getClanJoinedAt() {
        return clanJoinedAt;
    }

    public void setClanJoinedAt(LocalDateTime clanJoinedAt) {
        this.clanJoinedAt = clanJoinedAt;
    }

    public LocalDateTime getArenaDailyResetAt() {
        return arenaDailyResetAt;
    }

    public void setArenaDailyResetAt(LocalDateTime arenaDailyResetAt) {
        this.arenaDailyResetAt = arenaDailyResetAt;
    }

    private static UserType $default$userType() {
        return UserType.PLAYER;
    }

    private static int $default$tokenVersion() {
        return 0;
    }

    private static int $default$maxDigimonSlots() {
        return 1;
    }

    private static int $default$maxStorageSlots() {
        return 50;
    }

        private static int $default$arenaCoins() {
        return 0;
    }
    private static int $default$bits() {
        return 0;
    }
    private static int $default$unlockedIncubationSlots() {
        return 1;
    }

    private static int $default$digitalData() {
        return 0;
    }

    private static int $default$usernameChangeCount() {
        return 0;
    }

    public static class PlayerBuilder {
        private UUID id;
        private String username;
        private String email;
        private String password;
        private LocalDateTime createdAt;
        private DigitamaType selectedDigitama;
        private UUID activeDigimonId;
        private LocalDateTime lastMissionAt;
        private boolean starterSelected;
        private boolean userType$set;
        private UserType userType$value;
        private boolean tokenVersion$set;
        private int tokenVersion$value;
        private boolean maxDigimonSlots$set;
        private int maxDigimonSlots$value;
        private boolean maxStorageSlots$set;
        private int maxStorageSlots$value;
        private boolean arenaCoins$set;
        private int arenaCoins$value;
        private boolean bits$set;
        private int bits$value;
        private boolean unlockedIncubationSlots$set;
        private int unlockedIncubationSlots$value;
        private boolean digitalData$set;
        private int digitalData$value;
        private boolean usernameChangeCount$set;
        private int usernameChangeCount$value;
        private UUID clanId;
        private ClanRole clanRole;
        private LocalDateTime clanJoinedAt;
        private LocalDateTime arenaDailyResetAt;

        PlayerBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder username(final String username) {
            this.username = username;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder email(final String email) {
            this.email = email;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder password(final String password) {
            this.password = password;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder selectedDigitama(final DigitamaType selectedDigitama) {
            this.selectedDigitama = selectedDigitama;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder activeDigimonId(final UUID activeDigimonId) {
            this.activeDigimonId = activeDigimonId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder lastMissionAt(final LocalDateTime lastMissionAt) {
            this.lastMissionAt = lastMissionAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder starterSelected(final boolean starterSelected) {
            this.starterSelected = starterSelected;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder userType(final UserType userType) {
            this.userType$value = userType;
            userType$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder tokenVersion(final int tokenVersion) {
            this.tokenVersion$value = tokenVersion;
            tokenVersion$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder maxDigimonSlots(final int maxDigimonSlots) {
            this.maxDigimonSlots$value = maxDigimonSlots;
            maxDigimonSlots$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder maxStorageSlots(final int maxStorageSlots) {
            this.maxStorageSlots$value = maxStorageSlots;
            maxStorageSlots$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder arenaCoins(final int arenaCoins) {
            this.arenaCoins$value = arenaCoins;
            arenaCoins$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder bits(final int bits) {
            this.bits$value = bits;
            bits$set = true;
            return this;
        }

        public Player.PlayerBuilder unlockedIncubationSlots(final int unlockedIncubationSlots) {
            this.unlockedIncubationSlots$value = unlockedIncubationSlots;
            unlockedIncubationSlots$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder digitalData(final int digitalData) {
            this.digitalData$value = digitalData;
            digitalData$set = true;
            return this;
        }

        public Player.PlayerBuilder usernameChangeCount(final int usernameChangeCount) {
            this.usernameChangeCount$value = usernameChangeCount;
            usernameChangeCount$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder clanId(final UUID clanId) {
            this.clanId = clanId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder clanRole(final ClanRole clanRole) {
            this.clanRole = clanRole;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder clanJoinedAt(final LocalDateTime clanJoinedAt) {
            this.clanJoinedAt = clanJoinedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Player.PlayerBuilder arenaDailyResetAt(final LocalDateTime arenaDailyResetAt) {
            this.arenaDailyResetAt = arenaDailyResetAt;
            return this;
        }

        public Player build() {
            UserType userType$value = this.userType$value;
            if (!this.userType$set) userType$value = Player.$default$userType();
            int tokenVersion$value = this.tokenVersion$value;
            if (!this.tokenVersion$set) tokenVersion$value = Player.$default$tokenVersion();
            int maxDigimonSlots$value = this.maxDigimonSlots$value;
            if (!this.maxDigimonSlots$set) maxDigimonSlots$value = Player.$default$maxDigimonSlots();
            int maxStorageSlots$value = this.maxStorageSlots$value;
            if (!this.maxStorageSlots$set) maxStorageSlots$value = Player.$default$maxStorageSlots();
            int arenaCoins$value = this.arenaCoins$value;
            if (!this.arenaCoins$set) arenaCoins$value = Player.$default$arenaCoins();
            int bits$value = this.bits$value;
            if (!this.bits$set) bits$value = Player.$default$bits();
            int unlockedIncubationSlots$value = this.unlockedIncubationSlots$value;
            if (!this.unlockedIncubationSlots$set) unlockedIncubationSlots$value = Player.$default$unlockedIncubationSlots();
            int digitalData$value = this.digitalData$value;
            if (!this.digitalData$set) digitalData$value = Player.$default$digitalData();
            int usernameChangeCount$value = this.usernameChangeCount$value;
            if (!this.usernameChangeCount$set) usernameChangeCount$value = Player.$default$usernameChangeCount();
            return new Player(this.id, this.username, this.email, this.password, this.createdAt, this.selectedDigitama, this.activeDigimonId, this.lastMissionAt, this.starterSelected, userType$value, tokenVersion$value, maxDigimonSlots$value, maxStorageSlots$value, arenaCoins$value, bits$value, unlockedIncubationSlots$value, digitalData$value, usernameChangeCount$value, this.clanId, this.clanRole, this.clanJoinedAt, this.arenaDailyResetAt);
        }

        @Override
        public String toString() {
            return "Player.PlayerBuilder(id=" + this.id + ", username=" + this.username + ", email=" + this.email + ", password=" + this.password + ", createdAt=" + this.createdAt + ", selectedDigitama=" + this.selectedDigitama + ", activeDigimonId=" + this.activeDigimonId + ", lastMissionAt=" + this.lastMissionAt + ", starterSelected=" + this.starterSelected + ", userType$value=" + this.userType$value + ", tokenVersion$value=" + this.tokenVersion$value + ", maxDigimonSlots$value=" + this.maxDigimonSlots$value + ", maxStorageSlots$value=" + this.maxStorageSlots$value + ", arenaCoins=" + this.arenaCoins$value + ", unlockedIncubationSlots=" + this.unlockedIncubationSlots$value + ", clanId=" + this.clanId + ", clanRole=" + this.clanRole + ", clanJoinedAt=" + this.clanJoinedAt + ", arenaDailyResetAt=" + this.arenaDailyResetAt + ")";
        }
    }

    public static Player.PlayerBuilder builder() {
        return new Player.PlayerBuilder();
    }

    protected Player() {
        this.userType = Player.$default$userType();
        this.tokenVersion = Player.$default$tokenVersion();
        this.maxDigimonSlots = Player.$default$maxDigimonSlots();
        this.maxStorageSlots = Player.$default$maxStorageSlots();
        this.arenaCoins = Player.$default$arenaCoins();
        this.bits = Player.$default$bits();
        this.unlockedIncubationSlots = Player.$default$unlockedIncubationSlots();
        this.digitalData = Player.$default$digitalData();
        this.usernameChangeCount = Player.$default$usernameChangeCount();
    }

    public Player(final UUID id, final String username, final String email, final String password, final LocalDateTime createdAt, final DigitamaType selectedDigitama, final UUID activeDigimonId, final LocalDateTime lastMissionAt, final boolean starterSelected, final UserType userType, final int tokenVersion, final int maxDigimonSlots, final int maxStorageSlots, final int arenaCoins, final int bits, final int unlockedIncubationSlots, final int digitalData, final int usernameChangeCount, final UUID clanId, final ClanRole clanRole, final LocalDateTime clanJoinedAt, final LocalDateTime arenaDailyResetAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.selectedDigitama = selectedDigitama;
        this.activeDigimonId = activeDigimonId;
        this.lastMissionAt = lastMissionAt;
        this.starterSelected = starterSelected;
        this.userType = userType;
        this.tokenVersion = tokenVersion;
        this.maxDigimonSlots = maxDigimonSlots;
        this.maxStorageSlots = maxStorageSlots;
        this.arenaCoins = arenaCoins;
        this.bits = bits;
        this.unlockedIncubationSlots = unlockedIncubationSlots;
        this.digitalData = digitalData;
        this.usernameChangeCount = usernameChangeCount;
        this.clanId = clanId;
        this.clanRole = clanRole;
        this.clanJoinedAt = clanJoinedAt;
        this.arenaDailyResetAt = arenaDailyResetAt;
    }
}
