package com.dro.modules.boss.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "boss_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    /** Indica se o cooldown entre tentativas está ativo para este Boss. */
    @Column(name = "cooldown_enabled", nullable = false)
    @Builder.Default
    private boolean cooldownEnabled = true;

    @Column(name = "base_xp_reward", nullable = false)
    private int baseXpReward;

    @Column(name = "base_bits_reward", nullable = false)
    private int baseBitsReward;

    @Column(name = "defeat_xp_percent", nullable = false)
    private int defeatXpPercent;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    /** Baú concedido após uma vitória elegível contra este Boss. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chest_definition_id")
    private ChestDefinitionEntity chestDefinition;

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

    @OneToMany(mappedBy = "boss", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BossDropEntity> drops;
}
