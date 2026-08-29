package com.dro.modules.boss.domain;

import com.dro.modules.boss.world.domain.WorldBossRewardType;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.List;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "boss_definitions")
public class BossDefinitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "boss_type", nullable = false)
    private BossType bossType;
    @Enumerated(EnumType.STRING)
    @Column(name = "required_stage", nullable = false)
    private Stage requiredStage;
    @Column(name = "required_level", nullable = false)
    private int requiredLevel;
    @Column(name = "required_rebirths", nullable = false)
    private int requiredRebirths;
    @Column(nullable = false)
    private int hp;
    @Column(nullable = false)
    private int atk;
    @Column(nullable = false)
    private int def;
    @Column(name = "energy_cost", nullable = false)
    private int energyCost;
    @Column(name = "cooldown_minutes", nullable = false)
    private int cooldownMinutes;
    @Column(name = "base_xp_reward", nullable = false)
    private int baseXpReward;
    @Column(name = "base_bits_reward", nullable = false)
    private int baseBitsReward;
    @Column(name = "clan_honor_marks_reward", nullable = false)
    private int clanHonorMarksReward;
    @Column(name = "defeat_xp_percent", nullable = false)
    private int defeatXpPercent;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(nullable = false)
    private boolean active;
    /**
     * Baú concedido após uma vitória elegível contra um Boss normal/periódico.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chest_definition_id")
    private ChestDefinitionEntity chestDefinition;
    /**
     * Baú concedido por cada tentativa válida de Boss Mundial.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_attempt_chest_definition_id")
    private ChestDefinitionEntity worldAttemptChestDefinition;
    /**
     * Baú concedido ao participante com maior dano acumulado na derrota.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_top_damage_chest_definition_id")
    private ChestDefinitionEntity worldTopDamageChestDefinition;
    /**
     * Baú concedido ao jogador que desferiu o golpe final.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_final_blow_chest_definition_id")
    private ChestDefinitionEntity worldFinalBlowChestDefinition;

    @JsonProperty("chestCode")
    @Transient
    public String getChestCode() {
        return chestDefinition == null ? null : chestDefinition.getCode();
    }

    @JsonProperty("chestName")
    @Transient
    public String getChestName() {
        return chestDefinition == null ? null : chestDefinition.getName();
    }

    @JsonProperty("worldAttemptChestCode")
    @Transient
    public String getWorldAttemptChestCode() {
        return worldAttemptChestDefinition == null ? null : worldAttemptChestDefinition.getCode();
    }

    @JsonProperty("worldAttemptChestName")
    @Transient
    public String getWorldAttemptChestName() {
        return worldAttemptChestDefinition == null ? null : worldAttemptChestDefinition.getName();
    }

    @JsonProperty("worldTopDamageChestCode")
    @Transient
    public String getWorldTopDamageChestCode() {
        return worldTopDamageChestDefinition == null ? null : worldTopDamageChestDefinition.getCode();
    }

    @JsonProperty("worldTopDamageChestName")
    @Transient
    public String getWorldTopDamageChestName() {
        return worldTopDamageChestDefinition == null ? null : worldTopDamageChestDefinition.getName();
    }

    @JsonProperty("worldFinalBlowChestCode")
    @Transient
    public String getWorldFinalBlowChestCode() {
        return worldFinalBlowChestDefinition == null ? null : worldFinalBlowChestDefinition.getCode();
    }

    @JsonProperty("worldFinalBlowChestName")
    @Transient
    public String getWorldFinalBlowChestName() {
        return worldFinalBlowChestDefinition == null ? null : worldFinalBlowChestDefinition.getName();
    }

    public ChestDefinitionEntity chestForWorldReward(WorldBossRewardType rewardType) {
        return switch (rewardType) {
            case ATTEMPT -> worldAttemptChestDefinition;
            case TOP_DAMAGE -> worldTopDamageChestDefinition;
            case FINAL_BLOW -> worldFinalBlowChestDefinition;
        };
    }

    @OneToMany(mappedBy = "boss", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BossDropEntity> drops;

    public static class BossDefinitionEntityBuilder {
        private Long id;
        private String code;
        private String name;
        private BossType bossType;
        private Stage requiredStage;
        private int requiredLevel;
        private int requiredRebirths;
        private int hp;
        private int atk;
        private int def;
        private int energyCost;
        private int cooldownMinutes;
        private int baseXpReward;
        private int baseBitsReward;
        private int clanHonorMarksReward;
        private int defeatXpPercent;
        private String imageUrl;
        private boolean active;
        private ChestDefinitionEntity chestDefinition;
        private ChestDefinitionEntity worldAttemptChestDefinition;
        private ChestDefinitionEntity worldTopDamageChestDefinition;
        private ChestDefinitionEntity worldFinalBlowChestDefinition;
        private List<BossDropEntity> drops;

        BossDefinitionEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder bossType(final BossType bossType) {
            this.bossType = bossType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder requiredStage(final Stage requiredStage) {
            this.requiredStage = requiredStage;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder requiredLevel(final int requiredLevel) {
            this.requiredLevel = requiredLevel;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder requiredRebirths(final int requiredRebirths) {
            this.requiredRebirths = requiredRebirths;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder hp(final int hp) {
            this.hp = hp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder atk(final int atk) {
            this.atk = atk;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder def(final int def) {
            this.def = def;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder energyCost(final int energyCost) {
            this.energyCost = energyCost;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder cooldownMinutes(final int cooldownMinutes) {
            this.cooldownMinutes = cooldownMinutes;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder baseXpReward(final int baseXpReward) {
            this.baseXpReward = baseXpReward;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder baseBitsReward(final int baseBitsReward) {
            this.baseBitsReward = baseBitsReward;
            return this;
        }

        public BossDefinitionEntity.BossDefinitionEntityBuilder clanHonorMarksReward(final int clanHonorMarksReward) {
            this.clanHonorMarksReward = clanHonorMarksReward;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder defeatXpPercent(final int defeatXpPercent) {
            this.defeatXpPercent = defeatXpPercent;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder imageUrl(final String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder active(final boolean active) {
            this.active = active;
            return this;
        }

        /**
         * Baú concedido após uma vitória elegível contra um Boss normal/periódico.
         * @return {@code this}.
         */
        @JsonIgnore
        public BossDefinitionEntity.BossDefinitionEntityBuilder chestDefinition(final ChestDefinitionEntity chestDefinition) {
            this.chestDefinition = chestDefinition;
            return this;
        }

        /**
         * Baú concedido por cada tentativa válida de Boss Mundial.
         * @return {@code this}.
         */
        @JsonIgnore
        public BossDefinitionEntity.BossDefinitionEntityBuilder worldAttemptChestDefinition(final ChestDefinitionEntity worldAttemptChestDefinition) {
            this.worldAttemptChestDefinition = worldAttemptChestDefinition;
            return this;
        }

        /**
         * Baú concedido ao participante com maior dano acumulado na derrota.
         * @return {@code this}.
         */
        @JsonIgnore
        public BossDefinitionEntity.BossDefinitionEntityBuilder worldTopDamageChestDefinition(final ChestDefinitionEntity worldTopDamageChestDefinition) {
            this.worldTopDamageChestDefinition = worldTopDamageChestDefinition;
            return this;
        }

        /**
         * Baú concedido ao jogador que desferiu o golpe final.
         * @return {@code this}.
         */
        @JsonIgnore
        public BossDefinitionEntity.BossDefinitionEntityBuilder worldFinalBlowChestDefinition(final ChestDefinitionEntity worldFinalBlowChestDefinition) {
            this.worldFinalBlowChestDefinition = worldFinalBlowChestDefinition;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public BossDefinitionEntity.BossDefinitionEntityBuilder drops(final List<BossDropEntity> drops) {
            this.drops = drops;
            return this;
        }

        public BossDefinitionEntity build() {
            return new BossDefinitionEntity(this.id, this.code, this.name, this.bossType, this.requiredStage, this.requiredLevel, this.requiredRebirths, this.hp, this.atk, this.def, this.energyCost, this.cooldownMinutes, this.baseXpReward, this.baseBitsReward, this.clanHonorMarksReward, this.defeatXpPercent, this.imageUrl, this.active, this.chestDefinition, this.worldAttemptChestDefinition, this.worldTopDamageChestDefinition, this.worldFinalBlowChestDefinition, this.drops);
        }

        @Override
        public String toString() {
            return "BossDefinitionEntity.BossDefinitionEntityBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", bossType=" + this.bossType + ", requiredStage=" + this.requiredStage + ", requiredLevel=" + this.requiredLevel + ", requiredRebirths=" + this.requiredRebirths + ", hp=" + this.hp + ", atk=" + this.atk + ", def=" + this.def + ", energyCost=" + this.energyCost + ", cooldownMinutes=" + this.cooldownMinutes + ", baseXpReward=" + this.baseXpReward + ", baseBitsReward=" + this.baseBitsReward + ", defeatXpPercent=" + this.defeatXpPercent + ", imageUrl=" + this.imageUrl + ", active=" + this.active + ", chestDefinition=" + this.chestDefinition + ", worldAttemptChestDefinition=" + this.worldAttemptChestDefinition + ", worldTopDamageChestDefinition=" + this.worldTopDamageChestDefinition + ", worldFinalBlowChestDefinition=" + this.worldFinalBlowChestDefinition + ", drops=" + this.drops + ")";
        }
    }

    public static BossDefinitionEntity.BossDefinitionEntityBuilder builder() {
        return new BossDefinitionEntity.BossDefinitionEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public BossType getBossType() {
        return this.bossType;
    }

    public Stage getRequiredStage() {
        return this.requiredStage;
    }

    public int getRequiredLevel() {
        return this.requiredLevel;
    }

    public int getRequiredRebirths() {
        return this.requiredRebirths;
    }

    public int getHp() {
        return this.hp;
    }

    public int getAtk() {
        return this.atk;
    }

    public int getDef() {
        return this.def;
    }

    public int getEnergyCost() {
        return this.energyCost;
    }

    public int getCooldownMinutes() {
        return this.cooldownMinutes;
    }

    public int getBaseXpReward() {
        return this.baseXpReward;
    }

    public int getBaseBitsReward() {
        return this.baseBitsReward;
    }

    public int getClanHonorMarksReward() {
        return this.clanHonorMarksReward;
    }

    public int getDefeatXpPercent() {
        return this.defeatXpPercent;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public boolean isActive() {
        return this.active;
    }

    /**
     * Baú concedido após uma vitória elegível contra um Boss normal/periódico.
     */
    public ChestDefinitionEntity getChestDefinition() {
        return this.chestDefinition;
    }

    /**
     * Baú concedido por cada tentativa válida de Boss Mundial.
     */
    public ChestDefinitionEntity getWorldAttemptChestDefinition() {
        return this.worldAttemptChestDefinition;
    }

    /**
     * Baú concedido ao participante com maior dano acumulado na derrota.
     */
    public ChestDefinitionEntity getWorldTopDamageChestDefinition() {
        return this.worldTopDamageChestDefinition;
    }

    /**
     * Baú concedido ao jogador que desferiu o golpe final.
     */
    public ChestDefinitionEntity getWorldFinalBlowChestDefinition() {
        return this.worldFinalBlowChestDefinition;
    }

    public List<BossDropEntity> getDrops() {
        return this.drops;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setBossType(final BossType bossType) {
        this.bossType = bossType;
    }

    public void setRequiredStage(final Stage requiredStage) {
        this.requiredStage = requiredStage;
    }

    public void setRequiredLevel(final int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public void setRequiredRebirths(final int requiredRebirths) {
        this.requiredRebirths = requiredRebirths;
    }

    public void setHp(final int hp) {
        this.hp = hp;
    }

    public void setAtk(final int atk) {
        this.atk = atk;
    }

    public void setDef(final int def) {
        this.def = def;
    }

    public void setEnergyCost(final int energyCost) {
        this.energyCost = energyCost;
    }

    public void setCooldownMinutes(final int cooldownMinutes) {
        this.cooldownMinutes = cooldownMinutes;
    }

    public void setBaseXpReward(final int baseXpReward) {
        this.baseXpReward = baseXpReward;
    }

    public void setBaseBitsReward(final int baseBitsReward) {
        this.baseBitsReward = baseBitsReward;
    }

    public void setClanHonorMarksReward(final int clanHonorMarksReward) {
        this.clanHonorMarksReward = clanHonorMarksReward;
    }

    public void setDefeatXpPercent(final int defeatXpPercent) {
        this.defeatXpPercent = defeatXpPercent;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Baú concedido após uma vitória elegível contra um Boss normal/periódico.
     */
    public void setChestDefinition(final ChestDefinitionEntity chestDefinition) {
        this.chestDefinition = chestDefinition;
    }

    /**
     * Baú concedido por cada tentativa válida de Boss Mundial.
     */
    public void setWorldAttemptChestDefinition(final ChestDefinitionEntity worldAttemptChestDefinition) {
        this.worldAttemptChestDefinition = worldAttemptChestDefinition;
    }

    /**
     * Baú concedido ao participante com maior dano acumulado na derrota.
     */
    public void setWorldTopDamageChestDefinition(final ChestDefinitionEntity worldTopDamageChestDefinition) {
        this.worldTopDamageChestDefinition = worldTopDamageChestDefinition;
    }

    /**
     * Baú concedido ao jogador que desferiu o golpe final.
     */
    public void setWorldFinalBlowChestDefinition(final ChestDefinitionEntity worldFinalBlowChestDefinition) {
        this.worldFinalBlowChestDefinition = worldFinalBlowChestDefinition;
    }

    public void setDrops(final List<BossDropEntity> drops) {
        this.drops = drops;
    }

    public BossDefinitionEntity() {
    }

    /**
     * Creates a new {@code BossDefinitionEntity} instance.
     *
     * @param id
     * @param code
     * @param name
     * @param bossType
     * @param requiredStage
     * @param requiredLevel
     * @param requiredRebirths
     * @param hp
     * @param atk
     * @param def
     * @param energyCost
     * @param cooldownMinutes
     * @param baseXpReward
     * @param baseBitsReward
     * @param clanHonorMarksReward
     * @param defeatXpPercent
     * @param imageUrl
     * @param active
     * @param chestDefinition Baú concedido após uma vitória elegível contra um Boss normal/periódico.
     * @param worldAttemptChestDefinition Baú concedido por cada tentativa válida de Boss Mundial.
     * @param worldTopDamageChestDefinition Baú concedido ao participante com maior dano acumulado na derrota.
     * @param worldFinalBlowChestDefinition Baú concedido ao jogador que desferiu o golpe final.
     * @param drops
     */
    public BossDefinitionEntity(final Long id, final String code, final String name, final BossType bossType, final Stage requiredStage, final int requiredLevel, final int requiredRebirths, final int hp, final int atk, final int def, final int energyCost, final int cooldownMinutes, final int baseXpReward, final int baseBitsReward, final int clanHonorMarksReward, final int defeatXpPercent, final String imageUrl, final boolean active, final ChestDefinitionEntity chestDefinition, final ChestDefinitionEntity worldAttemptChestDefinition, final ChestDefinitionEntity worldTopDamageChestDefinition, final ChestDefinitionEntity worldFinalBlowChestDefinition, final List<BossDropEntity> drops) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.bossType = bossType;
        this.requiredStage = requiredStage;
        this.requiredLevel = requiredLevel;
        this.requiredRebirths = requiredRebirths;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.energyCost = energyCost;
        this.cooldownMinutes = cooldownMinutes;
        this.baseXpReward = baseXpReward;
        this.baseBitsReward = baseBitsReward;
        this.clanHonorMarksReward = clanHonorMarksReward;
        this.defeatXpPercent = defeatXpPercent;
        this.imageUrl = imageUrl;
        this.active = active;
        this.chestDefinition = chestDefinition;
        this.worldAttemptChestDefinition = worldAttemptChestDefinition;
        this.worldTopDamageChestDefinition = worldTopDamageChestDefinition;
        this.worldFinalBlowChestDefinition = worldFinalBlowChestDefinition;
        this.drops = drops;
    }
}
