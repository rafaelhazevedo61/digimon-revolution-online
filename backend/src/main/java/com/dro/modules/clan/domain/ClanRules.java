package com.dro.modules.clan.domain;

import com.dro.modules.player.domain.Player;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class ClanRules {

    public static final int MAX_NAME_LENGTH = 30;
    public static final int MIN_TAG_LENGTH = 2;
    public static final int MAX_TAG_LENGTH = 5;
    public static final int MAX_DESCRIPTION_LENGTH = 280;
    public static final int INITIAL_MAX_MEMBERS = 5;
    public static final int CREATE_COST = 0; // bits; 0 no MVP, ajustável aqui
    public static final int SLOT_BASE_COST = 500; // bits
    public static final int MAX_BOUGHT_SLOTS = 10; // limite de vagas extras compráveis
    public static final int MAX_LEVEL = 20;

    private static final List<LevelBonus> LEVEL_BONUSES = List.of(
            new LevelBonus(1, 0, 0),
            new LevelBonus(2, 1000, 1),
            new LevelBonus(3, 2500, 1),
            new LevelBonus(4, 5000, 1),
            new LevelBonus(5, 10000, 2),
            new LevelBonus(6, 18000, 1),
            new LevelBonus(7, 30000, 1),
            new LevelBonus(8, 48000, 1),
            new LevelBonus(9, 72000, 1),
            new LevelBonus(10, 105000, 1),
            new LevelBonus(11, 150000, 1),
            new LevelBonus(12, 205000, 1),
            new LevelBonus(13, 275000, 1),
            new LevelBonus(14, 360000, 1),
            new LevelBonus(15, 465000, 1),
            new LevelBonus(16, 590000, 1),
            new LevelBonus(17, 740000, 1),
            new LevelBonus(18, 920000, 1),
            new LevelBonus(19, 1140000, 1),
            new LevelBonus(20, 1400000, 1)
    );

    private static final Map<Integer, LevelBonus> BY_LEVEL =
            LEVEL_BONUSES.stream().collect(java.util.stream.Collectors.toMap(LevelBonus::level, b -> b));

    private static final Map<Integer, Integer> CUMULATIVE_MEMBERS_BONUS = buildCumulativeBonus();

    private static Map<Integer, Integer> buildCumulativeBonus() {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
        int cumulative = 0;
        for (LevelBonus bonus : LEVEL_BONUSES) {
            cumulative += bonus.maxMembersBonus();
            map.put(bonus.level(), cumulative);
        }
        return Map.copyOf(map);
    }

    public static boolean isNameValid(String name) {
        return name != null
                && !name.isBlank()
                && name.length() <= MAX_NAME_LENGTH
                && name.matches("^[\\p{L}0-9 _-]+$");
    }

    public static boolean isTagValid(String tag) {
        return tag != null
                && tag.length() >= MIN_TAG_LENGTH
                && tag.length() <= MAX_TAG_LENGTH
                && tag.matches("^[A-Za-z0-9]+$");
    }

    public static boolean isDescriptionValid(String description) {
        return description == null || description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    public static void validateCreateRequest(String name, String tag, String description) {
        if (!isNameValid(name)) {
            throw new BadRequestException("Clan name must be 1-30 characters and contain only letters, numbers, spaces, underscores or hyphens");
        }
        if (!isTagValid(tag)) {
            throw new BadRequestException("Clan tag must be " + MIN_TAG_LENGTH + "-" + MAX_TAG_LENGTH + " alphanumeric characters");
        }
        if (!isDescriptionValid(description)) {
            throw new BadRequestException("Clan description must be at most " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }

    public static int getMaxMembersForLevel(int level) {
        int capped = Math.min(level, MAX_LEVEL);
        int bonus = CUMULATIVE_MEMBERS_BONUS.getOrDefault(capped, 0);
        return INITIAL_MAX_MEMBERS + bonus;
    }

    public static int xpToNextLevel(int currentLevel, int currentXp) {
        if (currentLevel >= MAX_LEVEL) {
            return 0;
        }
        LevelBonus next = BY_LEVEL.get(currentLevel + 1);
        return Math.max(0, next.xpRequired() - currentXp);
    }

    public static int addExperience(Clan clan, int xp) {
        if (xp <= 0) {
            return 0;
        }
        clan.setExperience(clan.getExperience() + xp);
        int levelsGained = 0;
        while (clan.getLevel() < MAX_LEVEL) {
            LevelBonus next = BY_LEVEL.get(clan.getLevel() + 1);
            if (clan.getExperience() < next.xpRequired()) {
                break;
            }
            clan.setLevel(clan.getLevel() + 1);
            clan.setMaxMembers(getMaxMembersForLevel(clan.getLevel()));
            levelsGained++;
        }
        return levelsGained;
    }

    public static int slotCost(int boughtSlots) {
        if (boughtSlots >= MAX_BOUGHT_SLOTS) {
            return 0;
        }
        return SLOT_BASE_COST * (int) Math.pow(2, boughtSlots);
    }

    public static boolean isMaxSlotsReached(int boughtSlots) {
        return boughtSlots >= MAX_BOUGHT_SLOTS;
    }

    public static boolean canManageClanInfo(ClanRole role) {
        return role == ClanRole.LEADER || role == ClanRole.OFFICER;
    }

    public static boolean canKick(ClanRole actorRole, ClanRole targetRole) {
        if (actorRole == ClanRole.LEADER) {
            return targetRole != ClanRole.LEADER;
        }
        return actorRole == ClanRole.OFFICER && targetRole == ClanRole.MEMBER;
    }

    public static boolean canPromote(ClanRole actorRole, ClanRole targetRole) {
        return actorRole == ClanRole.LEADER && targetRole == ClanRole.MEMBER;
    }

    public static boolean canDemote(ClanRole actorRole, ClanRole targetRole) {
        return actorRole == ClanRole.LEADER && targetRole == ClanRole.OFFICER;
    }

    public static boolean canTransferLeadership(ClanRole actorRole) {
        return actorRole == ClanRole.LEADER;
    }

    public static boolean canDissolve(ClanRole actorRole) {
        return actorRole == ClanRole.LEADER;
    }

    public static void assertInClan(Player player, UUID clanId) {
        if (player.getClanId() == null || !player.getClanId().equals(clanId)) {
            throw new ForbiddenException("You are not a member of this clan");
        }
    }

    public static void assertLeader(Player player, UUID clanId) {
        assertInClan(player, clanId);
        if (player.getClanRole() != ClanRole.LEADER) {
            throw new ForbiddenException("Only the clan leader can do this");
        }
    }

    public static Clan create(String name, String tag, String description, UUID leaderId) {
        String normalizedName = name.trim();
        String normalizedTag = tag.toUpperCase().trim();
        String normalizedDescription = description != null ? description.trim() : null;
        validateCreateRequest(normalizedName, normalizedTag, normalizedDescription);
        LocalDateTime now = LocalDateTime.now();
        return Clan.builder()
                .id(UUID.randomUUID())
                .name(normalizedName)
                .tag(normalizedTag)
                .description(normalizedDescription)
                .leaderId(leaderId)
                .emblem(null)
                .maxMembers(INITIAL_MAX_MEMBERS)
                .level(1)
                .experience(0)
                .boughtSlots(0)
                .createdAt(now)
                .build();
    }

    public static int experienceForMission() { return 10; }
    public static int experienceForBoss() { return 25; }
    public static int experienceForArena(boolean victory) { return victory ? 15 : 5; }
    public static int experienceForRebirth() { return 100; }

    private record LevelBonus(int level, int xpRequired, int maxMembersBonus) {
    }
}
