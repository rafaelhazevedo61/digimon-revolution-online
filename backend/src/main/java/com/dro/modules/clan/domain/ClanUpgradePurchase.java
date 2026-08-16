package com.dro.modules.clan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "clan_upgrade_purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClanUpgradePurchase {

    @Id
    private UUID id;

    @Column(name = "clan_id", nullable = false)
    private UUID clanId;

    @Column(name = "upgrade_code", nullable = false, length = 30)
    private String upgradeCode;

    @Column(nullable = false)
    @Builder.Default
    private int level = 0;

    @Column(name = "total_spent_honor_marks", nullable = false)
    @Builder.Default
    private int totalSpentHonorMarks = 0;
}
