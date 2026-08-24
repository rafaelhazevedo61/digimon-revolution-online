package com.dro.modules.clan.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "clans")
public class Clan {
    @Id
    private UUID id;
    @Column(nullable = false, length = 30)
    private String name;
    @Column(nullable = false, length = 5)
    private String tag;
    @Column(length = 280)
    private String description;
    @Column(name = "leader_id", nullable = false)
    private UUID leaderId;
    @Column(length = 50)
    private String emblem;
    @Column(name = "max_members", nullable = false)
    private int maxMembers;
    @Column(nullable = false)
    private int level;
    @Column(nullable = false)
    private int experience;
    @Column(name = "honor_marks", nullable = false)
    private int honorMarks;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "dissolved_at")
    private LocalDateTime dissolvedAt;

    private static int $default$maxMembers() {
        return 5;
    }

    private static int $default$level() {
        return 1;
    }

    private static int $default$experience() {
        return 0;
    }

    private static int $default$honorMarks() {
        return 0;
    }

    private static boolean $default$active() {
        return true;
    }


    public static class ClanBuilder {
        private UUID id;
        private String name;
        private String tag;
        private String description;
        private UUID leaderId;
        private String emblem;
        private boolean maxMembers$set;
        private int maxMembers$value;
        private boolean level$set;
        private int level$value;
        private boolean experience$set;
        private int experience$value;
        private boolean honorMarks$set;
        private int honorMarks$value;
        private LocalDateTime createdAt;
        private boolean active$set;
        private boolean active$value;
        private LocalDateTime dissolvedAt;

        ClanBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder tag(final String tag) {
            this.tag = tag;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder leaderId(final UUID leaderId) {
            this.leaderId = leaderId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder emblem(final String emblem) {
            this.emblem = emblem;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder maxMembers(final int maxMembers) {
            this.maxMembers$value = maxMembers;
            maxMembers$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder level(final int level) {
            this.level$value = level;
            level$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder experience(final int experience) {
            this.experience$value = experience;
            experience$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder honorMarks(final int honorMarks) {
            this.honorMarks$value = honorMarks;
            honorMarks$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Clan.ClanBuilder dissolvedAt(final LocalDateTime dissolvedAt) {
            this.dissolvedAt = dissolvedAt;
            return this;
        }

        public Clan build() {
            int maxMembers$value = this.maxMembers$value;
            if (!this.maxMembers$set) maxMembers$value = Clan.$default$maxMembers();
            int level$value = this.level$value;
            if (!this.level$set) level$value = Clan.$default$level();
            int experience$value = this.experience$value;
            if (!this.experience$set) experience$value = Clan.$default$experience();
            int honorMarks$value = this.honorMarks$value;
            if (!this.honorMarks$set) honorMarks$value = Clan.$default$honorMarks();
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = Clan.$default$active();
            return new Clan(this.id, this.name, this.tag, this.description, this.leaderId, this.emblem, maxMembers$value, level$value, experience$value, honorMarks$value, this.createdAt, active$value, this.dissolvedAt);
        }

        @Override
        public String toString() {
            return "Clan.ClanBuilder(id=" + this.id + ", name=" + this.name + ", tag=" + this.tag + ", description=" + this.description + ", leaderId=" + this.leaderId + ", emblem=" + this.emblem + ", maxMembers$value=" + this.maxMembers$value + ", level$value=" + this.level$value + ", experience$value=" + this.experience$value + ", honorMarks$value=" + this.honorMarks$value + ", createdAt=" + this.createdAt + ", active$value=" + this.active$value + ", dissolvedAt=" + this.dissolvedAt + ")";
        }
    }

    public static Clan.ClanBuilder builder() {
        return new Clan.ClanBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getTag() {
        return this.tag;
    }

    public String getDescription() {
        return this.description;
    }

    public UUID getLeaderId() {
        return this.leaderId;
    }

    public String getEmblem() {
        return this.emblem;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public int getLevel() {
        return this.level;
    }

    public int getExperience() {
        return this.experience;
    }

    public int getHonorMarks() {
        return this.honorMarks;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public boolean isActive() {
        return this.active;
    }

    public LocalDateTime getDissolvedAt() {
        return this.dissolvedAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setLeaderId(final UUID leaderId) {
        this.leaderId = leaderId;
    }

    public void setEmblem(final String emblem) {
        this.emblem = emblem;
    }

    public void setMaxMembers(final int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    public void setExperience(final int experience) {
        this.experience = experience;
    }

    public void setHonorMarks(final int honorMarks) {
        this.honorMarks = honorMarks;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setDissolvedAt(final LocalDateTime dissolvedAt) {
        this.dissolvedAt = dissolvedAt;
    }

    public Clan() {
        this.maxMembers = Clan.$default$maxMembers();
        this.level = Clan.$default$level();
        this.experience = Clan.$default$experience();
        this.honorMarks = Clan.$default$honorMarks();
        this.active = Clan.$default$active();
    }

    public Clan(final UUID id, final String name, final String tag, final String description, final UUID leaderId, final String emblem, final int maxMembers, final int level, final int experience, final int honorMarks, final LocalDateTime createdAt, final boolean active, final LocalDateTime dissolvedAt) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.description = description;
        this.leaderId = leaderId;
        this.emblem = emblem;
        this.maxMembers = maxMembers;
        this.level = level;
        this.experience = experience;
        this.honorMarks = honorMarks;
        this.createdAt = createdAt;
        this.active = active;
        this.dissolvedAt = dissolvedAt;
    }
}
