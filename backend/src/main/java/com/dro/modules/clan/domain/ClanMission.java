package com.dro.modules.clan.domain;

import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "clan_missions")
public class ClanMission {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 30)
    private String code;
    @Column(nullable = false, length = 60)
    private String title;
    @Column(length = 280)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "objective_type", nullable = false, length = 30)
    private ClanMissionObjectiveType objectiveType;
    @Column(name = "target_value", nullable = false)
    private int targetValue;
    @Column(name = "min_honor_marks_reward", nullable = false)
    private int minHonorMarksReward;
    @Column(name = "max_honor_marks_reward", nullable = false)
    private int maxHonorMarksReward;
    @Column(name = "clan_xp_reward", nullable = false)
    private int clanXpReward;
    @Column(name = "min_clan_level", nullable = false)
    private int minClanLevel;

    private static int $default$minClanLevel() {
        return 1;
    }


    public static class ClanMissionBuilder {
        private UUID id;
        private String code;
        private String title;
        private String description;
        private ClanMissionObjectiveType objectiveType;
        private int targetValue;
        private int minHonorMarksReward;
        private int maxHonorMarksReward;
        private int clanXpReward;
        private boolean minClanLevel$set;
        private int minClanLevel$value;

        ClanMissionBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder title(final String title) {
            this.title = title;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder objectiveType(final ClanMissionObjectiveType objectiveType) {
            this.objectiveType = objectiveType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder targetValue(final int targetValue) {
            this.targetValue = targetValue;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder minHonorMarksReward(final int minHonorMarksReward) {
            this.minHonorMarksReward = minHonorMarksReward;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder maxHonorMarksReward(final int maxHonorMarksReward) {
            this.maxHonorMarksReward = maxHonorMarksReward;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder clanXpReward(final int clanXpReward) {
            this.clanXpReward = clanXpReward;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanMission.ClanMissionBuilder minClanLevel(final int minClanLevel) {
            this.minClanLevel$value = minClanLevel;
            minClanLevel$set = true;
            return this;
        }

        public ClanMission build() {
            int minClanLevel$value = this.minClanLevel$value;
            if (!this.minClanLevel$set) minClanLevel$value = ClanMission.$default$minClanLevel();
            return new ClanMission(this.id, this.code, this.title, this.description, this.objectiveType, this.targetValue, this.minHonorMarksReward, this.maxHonorMarksReward, this.clanXpReward, minClanLevel$value);
        }

        @Override
        public String toString() {
            return "ClanMission.ClanMissionBuilder(id=" + this.id + ", code=" + this.code + ", title=" + this.title + ", description=" + this.description + ", objectiveType=" + this.objectiveType + ", targetValue=" + this.targetValue + ", minHonorMarksReward=" + this.minHonorMarksReward + ", maxHonorMarksReward=" + this.maxHonorMarksReward + ", clanXpReward=" + this.clanXpReward + ", minClanLevel$value=" + this.minClanLevel$value + ")";
        }
    }

    public static ClanMission.ClanMissionBuilder builder() {
        return new ClanMission.ClanMissionBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public ClanMissionObjectiveType getObjectiveType() {
        return this.objectiveType;
    }

    public int getTargetValue() {
        return this.targetValue;
    }

    public int getMinHonorMarksReward() {
        return this.minHonorMarksReward;
    }

    public int getMaxHonorMarksReward() {
        return this.maxHonorMarksReward;
    }

    public int getClanXpReward() {
        return this.clanXpReward;
    }

    public int getMinClanLevel() {
        return this.minClanLevel;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setObjectiveType(final ClanMissionObjectiveType objectiveType) {
        this.objectiveType = objectiveType;
    }

    public void setTargetValue(final int targetValue) {
        this.targetValue = targetValue;
    }

    public void setMinHonorMarksReward(final int minHonorMarksReward) {
        this.minHonorMarksReward = minHonorMarksReward;
    }

    public void setMaxHonorMarksReward(final int maxHonorMarksReward) {
        this.maxHonorMarksReward = maxHonorMarksReward;
    }

    public void setClanXpReward(final int clanXpReward) {
        this.clanXpReward = clanXpReward;
    }

    public void setMinClanLevel(final int minClanLevel) {
        this.minClanLevel = minClanLevel;
    }

    public ClanMission() {
        this.minClanLevel = ClanMission.$default$minClanLevel();
    }

    public ClanMission(final UUID id, final String code, final String title, final String description, final ClanMissionObjectiveType objectiveType, final int targetValue, final int minHonorMarksReward, final int maxHonorMarksReward, final int clanXpReward, final int minClanLevel) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.objectiveType = objectiveType;
        this.targetValue = targetValue;
        this.minHonorMarksReward = minHonorMarksReward;
        this.maxHonorMarksReward = maxHonorMarksReward;
        this.clanXpReward = clanXpReward;
        this.minClanLevel = minClanLevel;
    }
}
