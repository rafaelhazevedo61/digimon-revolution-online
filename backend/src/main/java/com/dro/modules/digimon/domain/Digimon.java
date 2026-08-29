package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.*;
import com.dro.shared.exception.UnprocessableException;
import com.dro.modules.player.domain.Player;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Parceiro Digimon persistente e principal unidade de progressão do jogador.
 *
 * <p>O Digimon concentra estágio, nível, experiência, IVs, raridade,
 * personalidade, trait, energia, Rebirth e referências aos equipamentos
 * por slot. O saldo de Bits pertence ao Player; o campo legado é mantido apenas
 * durante a migração. Inventário e operações de combate devem respeitar o vínculo
 * entre este registro e seu {@code playerId}.</p>
 */
@Entity
@Table(name = "digimons")
public class Digimon {
    private static final int MAX_LEVEL = DigimonLevelRules.MAX_LEVEL;
    @Id
    private UUID id;
    @Column(name = "player_id", nullable = false)
    private UUID playerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;
    @Column(nullable = false)
    private int level;
    @Column(nullable = false)
    private int experience;
    private int hp;
    private int attack;
    private int defense;
    private int ivHp;
    private int ivAttack;
    private int ivDefense;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DigimonGrade grade;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity;
    @Column(name = "rarity_changed_by_die", nullable = false)
    private boolean rarityChangedByDie;
    @Enumerated(EnumType.STRING)
    @Column(name = "original_rarity_before_die")
    private Rarity originalRarityBeforeDie;
    @Column(name = "rarity_changed_by_die_at")
    private LocalDateTime rarityChangedByDieAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Personality personality;
    @Column(nullable = false)
    private int energy;
    @Column(name = "max_energy", nullable = false)
    private int maxEnergy;
    @Column(name = "last_energy_update", nullable = false)
    private Instant lastEnergyUpdate;
    /** Campo legado; o saldo oficial fica em Player.bits. */
    @Deprecated
    @Column(nullable = false)
    private int bits;
    @Column(name = "rebirth_count", nullable = false)
    private int rebirthCount;
    @Column(name = "arena_rating", nullable = false)
    private int arenaRating;
    @Column(name = "arena_wins", nullable = false)
    private int arenaWins;
    @Column(name = "arena_losses", nullable = false)
    private int arenaLosses;
    @Column(name = "is_bot", nullable = false)
    private boolean bot;
    @Version
    @Column(name = "version", nullable = false)
    private long version;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DigimonStatus status;
    @Column(name = "reborned_from")
    private UUID rebornedFrom;
    @Enumerated(EnumType.STRING)
    @Column(name = "trait")
    private Trait trait;
    @Column(name = "weapon_id")
    private UUID weaponId;
    @Column(name = "armor_id")
    private UUID armorId;
    @Column(name = "accessory_id")
    private UUID accessoryId;
    @Column(name = "digimon_info_id")
    private Long digimonInfoId;

    /**
     * Vincula uma arma ao slot de arma do Digimon.
     */
    public void equipWeapon(UUID equipmentId) {
        this.weaponId = equipmentId;
    }

    /**
     * Remove a arma atualmente vinculada.
     */
    public void unequipWeapon() {
        this.weaponId = null;
    }

    /**
     * Vincula uma armadura ao slot de armadura do Digimon.
     */
    public void equipArmor(UUID equipmentId) {
        this.armorId = equipmentId;
    }

    /**
     * Remove a armadura atualmente vinculada.
     */
    public void unequipArmor() {
        this.armorId = null;
    }

    /**
     * Vincula um acessório ao slot de acessório do Digimon.
     */
    public void equipAccessory(UUID equipmentId) {
        this.accessoryId = equipmentId;
    }

    /**
     * Remove o acessório atualmente vinculado.
     */
    public void unequipAccessory() {
        this.accessoryId = null;
    }

    /**
     * Retorna o equipamento vinculado ao slot informado.
     */
    public UUID getEquipmentIdBySlot(com.dro.modules.equipment.domain.EquipmentSlot slot) {
        return switch (slot) {
            case WEAPON -> weaponId;
            case ARMOR -> armorId;
            case ACCESSORY -> accessoryId;
        };
    }

    /**
     * Define ou substitui o equipamento vinculado ao slot informado.
     */
    public void setEquipmentBySlot(com.dro.modules.equipment.domain.EquipmentSlot slot, UUID equipmentId) {
        switch (slot) {
            case WEAPON -> this.weaponId = equipmentId;
            case ARMOR -> this.armorId = equipmentId;
            case ACCESSORY -> this.accessoryId = equipmentId;
        }
    }

    /**
     * Limpa a referência do equipamento no slot informado.
     */
    public void clearSlot(com.dro.modules.equipment.domain.EquipmentSlot slot) {
        setEquipmentBySlot(slot, null);
    }

    /**
     * Adiciona experiência após aplicar os multiplicadores de raridade, personalidade e trait.
     *
     * <p>O método pode realizar vários level ups e nunca ultrapassa o nível máximo
     * definido por {@link DigimonLevelRules}.</p>
     *
     * @param baseXp experiência base recebida pela atividade
     */
    public void gainExperience(int baseXp) {
        double rarityMultiplier = RarityRules.getXpMultiplier(this.rarity);
        double personalityMultiplier = PersonalityRules.getXpMultiplier(this.personality);
        double traitMultiplier = TraitRules.getXpMultiplier(this.trait);
        int finalXp = (int) Math.floor(baseXp * rarityMultiplier * personalityMultiplier * traitMultiplier);
        applyExperience(finalXp);
    }

    /**
     * Concede exatamente a quantidade informada, sem aplicar multiplicadores de
     * raridade, personalidade ou trait. É usado por itens de XP instantâneo.
     */
    public void grantDirectExperience(int xp) {
        applyExperience(xp);
    }

    /**
     * Retorna a XP necessária para o próximo nível, ou zero no nível máximo.
     */
    public int getExperienceToNextLevel() {
        return xpToNextLevel();
    }

    private void applyExperience(int xp) {
        if (xp <= 0 || this.level >= MAX_LEVEL) {
            return;
        }
        this.experience += xp;
        while (this.level < MAX_LEVEL) {
            int xpRequired = xpToNextLevel();
            if (this.experience < xpRequired) {
                break;
            }
            this.experience -= xpRequired;
            levelUp();
        }
        if (this.level > MAX_LEVEL) {
            this.level = MAX_LEVEL;
        }
    }

    private int xpToNextLevel() {
        return DigimonLevelRules.xpToNextLevel(this.level);
    }

    private void levelUp() {
        if (this.level >= MAX_LEVEL) {
            this.level = MAX_LEVEL;
            return;
        }
        this.level++;
        this.hp += 2;
        this.attack += 1;
        this.defense += 1;
    }

    /**
     * Regenera energia usando o limite máximo atual do Digimon.
     */
    public void regenerateEnergy() {
        regenerateEnergy(0);
    }

    /**
     * Regenera uma unidade de energia a cada cinco minutos completos.
     *
     * @param maxEnergyBonus bônus temporário ou de trait aplicado ao limite máximo
     */
    public void regenerateEnergy(int maxEnergyBonus) {
        int effectiveMax = maxEnergy + maxEnergyBonus;
        if (energy >= effectiveMax) return;
        Instant now = Instant.now();
        long minutesPassed = Duration.between(lastEnergyUpdate, now).toMinutes();
        long energyRecovered = minutesPassed / 5;
        if (energyRecovered > 0) {
            energy = (int) Math.min(effectiveMax, energy + energyRecovered);
            lastEnergyUpdate = lastEnergyUpdate.plus(Duration.ofMinutes(energyRecovered * 5));
        }
    }

    /**
     * Consome energia sem permitir saldo negativo.
     *
     * @param amount quantidade positiva a consumir
     * @throws UnprocessableException quando a energia disponível é insuficiente
     */
    public void consumeEnergy(int amount) {
        if (energy < amount) {
            throw new UnprocessableException("Energia insuficiente");
        }
        energy -= amount;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getIvHp() {
        return ivHp;
    }

    public void setIvHp(int ivHp) {
        this.ivHp = ivHp;
    }

    public int getIvAttack() {
        return ivAttack;
    }

    public void setIvAttack(int ivAttack) {
        this.ivAttack = ivAttack;
    }

    public int getIvDefense() {
        return ivDefense;
    }

    public void setIvDefense(int ivDefense) {
        this.ivDefense = ivDefense;
    }

    public DigimonGrade getGrade() {
        return grade;
    }

    public void setGrade(DigimonGrade grade) {
        this.grade = grade;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public boolean isRarityChangedByDie() {
        return rarityChangedByDie;
    }

    public Rarity getOriginalRarityBeforeDie() {
        return originalRarityBeforeDie;
    }

    public LocalDateTime getRarityChangedByDieAt() {
        return rarityChangedByDieAt;
    }

    public void markRarityChangedByDie(Rarity originalRarity, LocalDateTime changedAt) {
        if (!this.rarityChangedByDie) {
            this.originalRarityBeforeDie = originalRarity;
            this.rarityChangedByDieAt = changedAt;
            this.rarityChangedByDie = true;
        }
    }

    public Personality getPersonality() {
        return personality;
    }

    public void setPersonality(Personality personality) {
        this.personality = personality;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public Instant getLastEnergyUpdate() {
        return lastEnergyUpdate;
    }

    public void setLastEnergyUpdate(Instant lastEnergyUpdate) {
        this.lastEnergyUpdate = lastEnergyUpdate;
    }

    /**
     * Retorna o saldo global do jogador quando a relação está disponível.
     * O campo local permanece apenas como compatibilidade com fixtures legadas.
     */
    public int getBits() {
        return player != null ? player.getBits() : bits;
    }

    /**
     * Atualiza o saldo global do jogador quando a relação está disponível.
     */
    public void setBits(int bits) {
        if (player != null) {
            player.setBits(bits);
        } else {
            this.bits = bits;
        }
    }

    public int getRebirthCount() {
        return rebirthCount;
    }

    public void setRebirthCount(int rebirthCount) {
        this.rebirthCount = rebirthCount;
    }

    public int getArenaRating() {
        return arenaRating;
    }

    public void setArenaRating(int arenaRating) {
        this.arenaRating = arenaRating;
    }

    public int getArenaWins() {
        return arenaWins;
    }

    public void setArenaWins(int arenaWins) {
        this.arenaWins = arenaWins;
    }

    public int getArenaLosses() {
        return arenaLosses;
    }

    public void setArenaLosses(int arenaLosses) {
        this.arenaLosses = arenaLosses;
    }

    public boolean isBot() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public DigimonStatus getStatus() {
        return status;
    }

    public void setStatus(DigimonStatus status) {
        this.status = status;
    }

    public UUID getRebornedFrom() {
        return rebornedFrom;
    }

    public void setRebornedFrom(UUID rebornedFrom) {
        this.rebornedFrom = rebornedFrom;
    }

    public Trait getTrait() {
        return trait;
    }

    public void setTrait(Trait trait) {
        this.trait = trait;
    }

    public UUID getWeaponId() {
        return weaponId;
    }

    public void setWeaponId(UUID weaponId) {
        this.weaponId = weaponId;
    }

    public UUID getArmorId() {
        return armorId;
    }

    public void setArmorId(UUID armorId) {
        this.armorId = armorId;
    }

    public UUID getAccessoryId() {
        return accessoryId;
    }

    public void setAccessoryId(UUID accessoryId) {
        this.accessoryId = accessoryId;
    }

    public Long getDigimonInfoId() {
        return digimonInfoId;
    }

    public void setDigimonInfoId(Long digimonInfoId) {
        this.digimonInfoId = digimonInfoId;
    }

    private static int $default$arenaRating() {
        return 1000;
    }

    private static int $default$arenaWins() {
        return 0;
    }

    private static int $default$arenaLosses() {
        return 0;
    }

    private static boolean $default$bot() {
        return false;
    }

    private static long $default$version() {
        return 0;
    }


    public static class DigimonBuilder {
        private UUID id;
        private UUID playerId;
        private String name;
        private String type;
        private Stage stage;
        private int level;
        private int experience;
        private int hp;
        private int attack;
        private int defense;
        private int ivHp;
        private int ivAttack;
        private int ivDefense;
        private DigimonGrade grade;
        private LocalDateTime createdAt;
        private Rarity rarity;
        private Personality personality;
        private int energy;
        private int maxEnergy;
        private Instant lastEnergyUpdate;
        private int bits;
        private int rebirthCount;
        private boolean arenaRating$set;
        private int arenaRating$value;
        private boolean arenaWins$set;
        private int arenaWins$value;
        private boolean arenaLosses$set;
        private int arenaLosses$value;
        private boolean bot$set;
        private boolean bot$value;
        private boolean version$set;
        private long version$value;
        private DigimonStatus status;
        private UUID rebornedFrom;
        private Trait trait;
        private UUID weaponId;
        private UUID armorId;
        private UUID accessoryId;
        private Long digimonInfoId;

        DigimonBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder type(final String type) {
            this.type = type;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder stage(final Stage stage) {
            this.stage = stage;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder level(final int level) {
            this.level = level;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder experience(final int experience) {
            this.experience = experience;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder hp(final int hp) {
            this.hp = hp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder attack(final int attack) {
            this.attack = attack;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder defense(final int defense) {
            this.defense = defense;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder ivHp(final int ivHp) {
            this.ivHp = ivHp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder ivAttack(final int ivAttack) {
            this.ivAttack = ivAttack;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder ivDefense(final int ivDefense) {
            this.ivDefense = ivDefense;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder grade(final DigimonGrade grade) {
            this.grade = grade;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder rarity(final Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder personality(final Personality personality) {
            this.personality = personality;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder energy(final int energy) {
            this.energy = energy;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder maxEnergy(final int maxEnergy) {
            this.maxEnergy = maxEnergy;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder lastEnergyUpdate(final Instant lastEnergyUpdate) {
            this.lastEnergyUpdate = lastEnergyUpdate;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder bits(final int bits) {
            this.bits = bits;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder rebirthCount(final int rebirthCount) {
            this.rebirthCount = rebirthCount;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder arenaRating(final int arenaRating) {
            this.arenaRating$value = arenaRating;
            arenaRating$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder arenaWins(final int arenaWins) {
            this.arenaWins$value = arenaWins;
            arenaWins$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder arenaLosses(final int arenaLosses) {
            this.arenaLosses$value = arenaLosses;
            arenaLosses$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder bot(final boolean bot) {
            this.bot$value = bot;
            bot$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder version(final long version) {
            this.version$value = version;
            version$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder status(final DigimonStatus status) {
            this.status = status;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder rebornedFrom(final UUID rebornedFrom) {
            this.rebornedFrom = rebornedFrom;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder trait(final Trait trait) {
            this.trait = trait;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder weaponId(final UUID weaponId) {
            this.weaponId = weaponId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder armorId(final UUID armorId) {
            this.armorId = armorId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder accessoryId(final UUID accessoryId) {
            this.accessoryId = accessoryId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Digimon.DigimonBuilder digimonInfoId(final Long digimonInfoId) {
            this.digimonInfoId = digimonInfoId;
            return this;
        }

        public Digimon build() {
            int arenaRating$value = this.arenaRating$value;
            if (!this.arenaRating$set) arenaRating$value = Digimon.$default$arenaRating();
            int arenaWins$value = this.arenaWins$value;
            if (!this.arenaWins$set) arenaWins$value = Digimon.$default$arenaWins();
            int arenaLosses$value = this.arenaLosses$value;
            if (!this.arenaLosses$set) arenaLosses$value = Digimon.$default$arenaLosses();
            boolean bot$value = this.bot$value;
            if (!this.bot$set) bot$value = Digimon.$default$bot();
            long version$value = this.version$value;
            if (!this.version$set) version$value = Digimon.$default$version();
            return new Digimon(this.id, this.playerId, this.name, this.type, this.stage, this.level, this.experience, this.hp, this.attack, this.defense, this.ivHp, this.ivAttack, this.ivDefense, this.grade, this.createdAt, this.rarity, this.personality, this.energy, this.maxEnergy, this.lastEnergyUpdate, this.bits, this.rebirthCount, arenaRating$value, arenaWins$value, arenaLosses$value, bot$value, version$value, this.status, this.rebornedFrom, this.trait, this.weaponId, this.armorId, this.accessoryId, this.digimonInfoId);
        }

        @Override
        public String toString() {
            return "Digimon.DigimonBuilder(id=" + this.id + ", playerId=" + this.playerId + ", name=" + this.name + ", type=" + this.type + ", stage=" + this.stage + ", level=" + this.level + ", experience=" + this.experience + ", hp=" + this.hp + ", attack=" + this.attack + ", defense=" + this.defense + ", ivHp=" + this.ivHp + ", ivAttack=" + this.ivAttack + ", ivDefense=" + this.ivDefense + ", grade=" + this.grade + ", createdAt=" + this.createdAt + ", rarity=" + this.rarity + ", personality=" + this.personality + ", energy=" + this.energy + ", maxEnergy=" + this.maxEnergy + ", lastEnergyUpdate=" + this.lastEnergyUpdate + ", bits=" + this.bits + ", rebirthCount=" + this.rebirthCount + ", arenaRating$value=" + this.arenaRating$value + ", arenaWins$value=" + this.arenaWins$value + ", arenaLosses$value=" + this.arenaLosses$value + ", bot$value=" + this.bot$value + ", version$value=" + this.version$value + ", status=" + this.status + ", rebornedFrom=" + this.rebornedFrom + ", trait=" + this.trait + ", weaponId=" + this.weaponId + ", armorId=" + this.armorId + ", accessoryId=" + this.accessoryId + ", digimonInfoId=" + this.digimonInfoId + ")";
        }
    }

    public static Digimon.DigimonBuilder builder() {
        return new Digimon.DigimonBuilder();
    }

    public Digimon() {
        this.arenaRating = Digimon.$default$arenaRating();
        this.arenaWins = Digimon.$default$arenaWins();
        this.arenaLosses = Digimon.$default$arenaLosses();
        this.bot = Digimon.$default$bot();
        this.version = Digimon.$default$version();
    }

    public Digimon(final UUID id, final UUID playerId, final String name, final String type, final Stage stage, final int level, final int experience, final int hp, final int attack, final int defense, final int ivHp, final int ivAttack, final int ivDefense, final DigimonGrade grade, final LocalDateTime createdAt, final Rarity rarity, final Personality personality, final int energy, final int maxEnergy, final Instant lastEnergyUpdate, final int bits, final int rebirthCount, final int arenaRating, final int arenaWins, final int arenaLosses, final boolean bot, final long version, final DigimonStatus status, final UUID rebornedFrom, final Trait trait, final UUID weaponId, final UUID armorId, final UUID accessoryId, final Long digimonInfoId) {
        this.id = id;
        this.playerId = playerId;
        this.name = name;
        this.type = type;
        this.stage = stage;
        this.level = level;
        this.experience = experience;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.ivHp = ivHp;
        this.ivAttack = ivAttack;
        this.ivDefense = ivDefense;
        this.grade = grade;
        this.createdAt = createdAt;
        this.rarity = rarity;
        this.personality = personality;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.lastEnergyUpdate = lastEnergyUpdate;
        this.bits = bits;
        this.rebirthCount = rebirthCount;
        this.arenaRating = arenaRating;
        this.arenaWins = arenaWins;
        this.arenaLosses = arenaLosses;
        this.bot = bot;
        this.version = version;
        this.status = status;
        this.rebornedFrom = rebornedFrom;
        this.trait = trait;
        this.weaponId = weaponId;
        this.armorId = armorId;
        this.accessoryId = accessoryId;
        this.digimonInfoId = digimonInfoId;
    }
}
