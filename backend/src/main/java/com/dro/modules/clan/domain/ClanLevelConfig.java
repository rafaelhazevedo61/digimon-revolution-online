package com.dro.modules.clan.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Componente da camada de configuração compartilhada do módulo de Clãs.
 */
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
