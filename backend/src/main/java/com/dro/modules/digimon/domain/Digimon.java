package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.*;
import com.dro.shared.exception.UnprocessableException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Parceiro Digimon persistente e principal unidade de progressão do jogador.
 *
 * <p>O Digimon concentra estágio, nível, experiência, IVs, raridade,
 * personalidade, trait, energia, Bits, Rebirth e referências aos equipamentos
 * por slot. Inventário e operações de combate devem respeitar o vínculo entre
 * este registro e seu {@code playerId}.</p>
 */
@Entity
@Table(name = "digimons")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Digimon {

    private static final int MAX_LEVEL = DigimonLevelRules.MAX_LEVEL;

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Personality personality;

    @Column(nullable = false)
    private int energy;

    @Column(name = "max_energy", nullable = false)
    private int maxEnergy;

    @Column(name = "last_energy_update", nullable = false)
    private Instant lastEnergyUpdate;

    @Column(nullable = false)
    private int bits;

    @Column(name = "rebirth_count", nullable = false)
    private int rebirthCount;

    @Column(name = "arena_rating", nullable = false)
    @Builder.Default
    private int arenaRating = 1000;

    @Column(name = "arena_wins", nullable = false)
    @Builder.Default
    private int arenaWins = 0;

    @Column(name = "arena_losses", nullable = false)
    @Builder.Default
    private int arenaLosses = 0;

    @Column(name = "is_bot", nullable = false)
    @Builder.Default
    private boolean bot = false;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private long version = 0;

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

    /** Vincula uma arma ao slot de arma do Digimon. */
    public void equipWeapon(UUID equipmentId) { this.weaponId = equipmentId; }

    /** Remove a arma atualmente vinculada. */
    public void unequipWeapon() { this.weaponId = null; }

    /** Vincula uma armadura ao slot de armadura do Digimon. */
    public void equipArmor(UUID equipmentId) { this.armorId = equipmentId; }

    /** Remove a armadura atualmente vinculada. */
    public void unequipArmor() { this.armorId = null; }

    /** Vincula um acessório ao slot de acessório do Digimon. */
    public void equipAccessory(UUID equipmentId) { this.accessoryId = equipmentId; }

    /** Remove o acessório atualmente vinculado. */
    public void unequipAccessory() { this.accessoryId = null; }

    /** Retorna o equipamento vinculado ao slot informado. */
    public UUID getEquipmentIdBySlot(com.dro.modules.equipment.domain.EquipmentSlot slot) {
        return switch (slot) {
            case WEAPON -> weaponId;
            case ARMOR -> armorId;
            case ACCESSORY -> accessoryId;
        };
    }

    /** Define ou substitui o equipamento vinculado ao slot informado. */
    public void setEquipmentBySlot(com.dro.modules.equipment.domain.EquipmentSlot slot, UUID equipmentId) {
        switch (slot) {
            case WEAPON -> this.weaponId = equipmentId;
            case ARMOR -> this.armorId = equipmentId;
            case ACCESSORY -> this.accessoryId = equipmentId;
        }
    }

    /** Limpa a referência do equipamento no slot informado. */
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
        double personalityMultiplier =
                PersonalityRules.getXpMultiplier(this.personality);
        double traitMultiplier = TraitRules.getXpMultiplier(this.trait);

        int finalXp = (int) Math.floor(
                baseXp
                        * rarityMultiplier
                        * personalityMultiplier
                        * traitMultiplier
        );

        this.experience += finalXp;

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

    /** Regenera energia usando o limite máximo atual do Digimon. */
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

            lastEnergyUpdate = lastEnergyUpdate.plus(
                    Duration.ofMinutes(energyRecovered * 5)
            );
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

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public Stage getStage () {
        return stage;
    }

    public void setStage (Stage stage) {
        this.stage = stage;
    }

    public int getLevel () {
        return level;
    }

    public void setLevel (int level) {
        this.level = level;
    }

    public int getExperience () {
        return experience;
    }

    public void setExperience (int experience) {
        this.experience = experience;
    }

    public int getHp () {
        return hp;
    }

    public void setHp (int hp) {
        this.hp = hp;
    }

    public int getAttack () {
        return attack;
    }

    public void setAttack (int attack) {
        this.attack = attack;
    }

    public int getDefense () {
        return defense;
    }

    public void setDefense (int defense) {
        this.defense = defense;
    }

    public int getIvHp () {
        return ivHp;
    }

    public void setIvHp (int ivHp) {
        this.ivHp = ivHp;
    }

    public int getIvAttack () {
        return ivAttack;
    }

    public void setIvAttack (int ivAttack) {
        this.ivAttack = ivAttack;
    }

    public int getIvDefense () {
        return ivDefense;
    }

    public void setIvDefense (int ivDefense) {
        this.ivDefense = ivDefense;
    }

    public DigimonGrade getGrade () {
        return grade;
    }

    public void setGrade (DigimonGrade grade) {
        this.grade = grade;
    }

    public LocalDateTime getCreatedAt () {
        return createdAt;
    }

    public void setCreatedAt (LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Rarity getRarity () {
        return rarity;
    }

    public void setRarity (Rarity rarity) {
        this.rarity = rarity;
    }

    public Personality getPersonality () {
        return personality;
    }

    public void setPersonality (Personality personality) {
        this.personality = personality;
    }

    public int getEnergy () {
        return energy;
    }

    public void setEnergy (int energy) {
        this.energy = energy;
    }

    public int getMaxEnergy () {
        return maxEnergy;
    }

    public void setMaxEnergy (int maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public Instant getLastEnergyUpdate () {
        return lastEnergyUpdate;
    }

    public void setLastEnergyUpdate (Instant lastEnergyUpdate) {
        this.lastEnergyUpdate = lastEnergyUpdate;
    }

    public int getBits () {
        return bits;
    }

    public void setBits (int bits) {
        this.bits = bits;
    }

    public int getRebirthCount () {
        return rebirthCount;
    }

    public void setRebirthCount (int rebirthCount) {
        this.rebirthCount = rebirthCount;
    }

    public int getArenaRating () {
        return arenaRating;
    }

    public void setArenaRating (int arenaRating) {
        this.arenaRating = arenaRating;
    }

    public int getArenaWins () {
        return arenaWins;
    }

    public void setArenaWins (int arenaWins) {
        this.arenaWins = arenaWins;
    }

    public int getArenaLosses () {
        return arenaLosses;
    }

    public void setArenaLosses (int arenaLosses) {
        this.arenaLosses = arenaLosses;
    }

    public boolean isBot () {
        return bot;
    }

    public void setBot (boolean bot) {
        this.bot = bot;
    }

    public long getVersion () {
        return version;
    }

    public void setVersion (long version) {
        this.version = version;
    }

    public DigimonStatus getStatus () {
        return status;
    }

    public void setStatus (DigimonStatus status) {
        this.status = status;
    }

    public UUID getRebornedFrom () {
        return rebornedFrom;
    }

    public void setRebornedFrom (UUID rebornedFrom) {
        this.rebornedFrom = rebornedFrom;
    }

    public Trait getTrait () {
        return trait;
    }

    public void setTrait (Trait trait) {
        this.trait = trait;
    }

    public UUID getWeaponId () {
        return weaponId;
    }

    public void setWeaponId (UUID weaponId) {
        this.weaponId = weaponId;
    }

    public UUID getArmorId () {
        return armorId;
    }

    public void setArmorId (UUID armorId) {
        this.armorId = armorId;
    }

    public UUID getAccessoryId () {
        return accessoryId;
    }

    public void setAccessoryId (UUID accessoryId) {
        this.accessoryId = accessoryId;
    }

    public Long getDigimonInfoId () {
        return digimonInfoId;
    }

    public void setDigimonInfoId (Long digimonInfoId) {
        this.digimonInfoId = digimonInfoId;
    }
}