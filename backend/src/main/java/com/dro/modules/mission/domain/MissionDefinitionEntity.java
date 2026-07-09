package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mission_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionDefinitionEntity implements Persistable<String> {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Area area;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_stage", nullable = false)
    private Stage requiredStage;

    @Column(name = "required_level", nullable = false)
    private int requiredLevel;

    @Column(name = "base_xp", nullable = false)
    private int baseXp;

    @Column(name = "base_bits", nullable = false)
    @Builder.Default
    private int baseBits = 0;

    @Column(name = "energy_cost", nullable = false)
    private int energyCost;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionRewardEntity> rewards = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionLootChanceEntity> lootChances = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MissionLootItemEntity> lootItems = new ArrayList<>();

    @Transient
    @Builder.Default
    private boolean newEntity = false;

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
