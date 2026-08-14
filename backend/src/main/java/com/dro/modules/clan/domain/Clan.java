package com.dro.modules.clan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clan {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false, unique = true, length = 5)
    private String tag;

    @Column(length = 280)
    private String description;

    @Column(name = "leader_id", nullable = false)
    private UUID leaderId;

    @Column(length = 50)
    private String emblem;

    @Column(name = "max_members", nullable = false)
    @Builder.Default
    private int maxMembers = 5;

    @Column(nullable = false)
    @Builder.Default
    private int level = 1;

    @Column(nullable = false)
    @Builder.Default
    private int experience = 0;

    @Column(name = "bought_slots", nullable = false)
    @Builder.Default
    private int boughtSlots = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public int getEffectiveMaxMembers() {
        return maxMembers + boughtSlots;
    }
}
