package com.dro.modules.clan.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClanRulesTest {

    @Test
    void validNamesAndTags() {
        assertTrue(ClanRules.isNameValid("DRO Heroes"));
        assertTrue(ClanRules.isTagValid("DRO"));
        assertTrue(ClanRules.isTagValid("ab"));
    }

    @Test
    void invalidNamesAndTags() {
        assertFalse(ClanRules.isNameValid(""));
        assertFalse(ClanRules.isNameValid("a".repeat(31)));
        assertFalse(ClanRules.isTagValid("a"));
        assertFalse(ClanRules.isTagValid("ab cd"));
        assertFalse(ClanRules.isTagValid("toolong"));
    }

    @Test
    void createClanNormalizesInput() {
        UUID leaderId = UUID.randomUUID();
        Clan clan = ClanRules.create("  My Clan  ", " mc ", "A cool clan", leaderId);

        assertEquals("My Clan", clan.getName());
        assertEquals("MC", clan.getTag());
        assertEquals("A cool clan", clan.getDescription());
        assertEquals(leaderId, clan.getLeaderId());
        assertEquals(1, clan.getLevel());
        assertEquals(5, clan.getEffectiveMaxMembers());
    }

    @Test
    void maxMembersGrowsWithLevel() {
        assertEquals(5, ClanRules.getMaxMembersForLevel(1));
        assertEquals(6, ClanRules.getMaxMembersForLevel(2));
        assertEquals(10, ClanRules.getMaxMembersForLevel(5));
        assertEquals(15, ClanRules.getMaxMembersForLevel(10));
        assertEquals(25, ClanRules.getMaxMembersForLevel(20));
    }

    @Test
    void addExperienceLevelsUpClan() {
        Clan clan = Clan.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .tag("TST")
                .leaderId(UUID.randomUUID())
                .maxMembers(ClanRules.INITIAL_MAX_MEMBERS)
                .level(1)
                .experience(0)
                .boughtSlots(0)
                .build();

        int levels = ClanRules.addExperience(clan, 1500);

        assertEquals(1, levels);
        assertEquals(2, clan.getLevel());
        assertEquals(6, clan.getEffectiveMaxMembers());
        assertTrue(clan.getExperience() >= 1500);
    }

    @Test
    void xpToNextLevel() {
        assertEquals(1000, ClanRules.xpToNextLevel(1, 0));
        assertEquals(0, ClanRules.xpToNextLevel(20, 1_500_000));
    }

    @Test
    void slotCostScalesExponentially() {
        assertEquals(500, ClanRules.slotCost(0));
        assertEquals(1000, ClanRules.slotCost(1));
        assertEquals(2000, ClanRules.slotCost(2));
        assertEquals(4000, ClanRules.slotCost(3));
    }

    @Test
    void permissions() {
        assertTrue(ClanRules.canManageClanInfo(ClanRole.OFFICER));
        assertTrue(ClanRules.canKick(ClanRole.OFFICER, ClanRole.MEMBER));
        assertFalse(ClanRules.canKick(ClanRole.OFFICER, ClanRole.OFFICER));
        assertTrue(ClanRules.canPromote(ClanRole.LEADER, ClanRole.MEMBER));
        assertTrue(ClanRules.canDemote(ClanRole.LEADER, ClanRole.OFFICER));
        assertFalse(ClanRules.canDissolve(ClanRole.OFFICER));
    }
}
