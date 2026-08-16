package com.dro.modules.clan.domain;

import com.dro.modules.clan.domain.enums.ClanUpgradeEffectType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "clan_upgrade_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClanUpgradeType {

    @Id
    @Column(name = "code", length = 30)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 280)
    private String description;

    @Column(name = "unlocked_at_clan_level", nullable = false)
    private int unlockedAtClanLevel;

    @Column(name = "max_level", nullable = false)
    @Builder.Default
    private int maxLevel = 10;

    @Column(name = "base_honor_marks_cost", nullable = false)
    private int baseHonorMarksCost;

    @Column(name = "cost_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal costMultiplier;

    @Column(name = "effect_per_level", nullable = false, precision = 5, scale = 4)
    private BigDecimal effectPerLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false, length = 30)
    private ClanUpgradeEffectType effectType;

    @Column(length = 30)
    private String stat;
}
