package com.dro.modules.clan.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clan_level_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClanLevelConfig {

    @Id
    private int level;

    @Column(name = "xp_required", nullable = false)
    private int xpRequired;

    @Column(name = "max_members_bonus", nullable = false)
    private int maxMembersBonus;
}
