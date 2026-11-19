package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanUpgradeResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanUpgradePurchase;
import com.dro.modules.clan.domain.ClanUpgradeType;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.ClanUpgradePurchaseRepository;
import com.dro.modules.clan.infra.ClanUpgradeTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanBonusServiceTest {

    @Mock private ClanRepository clanRepository;
    @Mock private ClanUpgradeTypeRepository upgradeTypeRepository;
    @Mock private ClanUpgradePurchaseRepository upgradePurchaseRepository;

    @InjectMocks
    private ClanBonusService service;

    private Clan makeClan(int level) {
        return Clan.builder()
                .id(UUID.randomUUID())
                .name("Test Clan")
                .tag("TST")
                .leaderId(UUID.randomUUID())
                .maxMembers(5)
                .level(level)
                .honorMarks(1000)
                .build();
    }

    private ClanUpgradeType makeType(String code, int baseCost, double multiplier, double effect) {
        return ClanUpgradeType.builder()
                .code(code)
                .name(code)
                .description("desc")
                .unlockedAtClanLevel(1)
                .maxLevel(10)
                .baseHonorMarksCost(baseCost)
                .costMultiplier(BigDecimal.valueOf(multiplier))
                .effectPerLevel(BigDecimal.valueOf(effect))
                .build();
    }

    @Test
    void calculateNextCost_returnsBaseAtLevelZero() {
        ClanUpgradeType type = makeType("ATTACK_BONUS", 150, 1.8, 0.01);

        assertEquals(150, service.calculateNextCost(type, 0));
    }

    @Test
    void calculateNextCost_scalesWithMultiplier() {
        ClanUpgradeType type = makeType("ATTACK_BONUS", 150, 1.8, 0.01);

        assertEquals(270, service.calculateNextCost(type, 1));
        assertEquals(486, service.calculateNextCost(type, 2));
    }

    @Test
    void getUpgradeLevel_andAttackBonusWork() {
        UUID clanId = UUID.randomUUID();
        ClanUpgradeType type = makeType("ATTACK_BONUS", 150, 1.8, 0.01);

        when(upgradeTypeRepository.findById("ATTACK_BONUS")).thenReturn(Optional.of(type));
        when(upgradePurchaseRepository.findByClanIdAndUpgradeCode(clanId, "ATTACK_BONUS"))
                .thenReturn(Optional.of(ClanUpgradePurchase.builder()
                        .id(UUID.randomUUID())
                        .clanId(clanId)
                        .upgradeCode("ATTACK_BONUS")
                        .level(3)
                        .build()));

        assertEquals(3, service.getUpgradeLevel(clanId, "ATTACK_BONUS"));
        assertEquals(0.03, service.getAttackBonusPercent(clanId), 0.0001);
    }

    @Test
    void getEffectiveMaxMembers_includesCapacityUpgrade() {
        Clan clan = makeClan(1);
        UUID clanId = clan.getId();

        when(upgradePurchaseRepository.findByClanIdAndUpgradeCode(clanId, "MEMBER_CAPACITY"))
                .thenReturn(Optional.of(ClanUpgradePurchase.builder()
                        .id(UUID.randomUUID())
                        .clanId(clanId)
                        .upgradeCode("MEMBER_CAPACITY")
                        .level(4)
                        .build()));

        assertEquals(9, service.getEffectiveMaxMembers(clan));
    }

    @Test
    void listUpgrades_returnsUnlockedAndMaxedInfo() {
        Clan clan = makeClan(3);
        ClanUpgradeType attack = makeType("ATTACK_BONUS", 150, 1.8, 0.01);
        attack.setUnlockedAtClanLevel(2);
        ClanUpgradeType defense = makeType("DEFENSE_BONUS", 150, 1.8, 0.01);
        defense.setUnlockedAtClanLevel(5);

        when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
        when(upgradeTypeRepository.findAll()).thenReturn(List.of(attack, defense));
        when(upgradePurchaseRepository.findByClanId(clan.getId())).thenReturn(List.of());

        List<ClanUpgradeResponse> responses = service.listUpgrades(clan.getId());

        assertEquals(2, responses.size());
        ClanUpgradeResponse attackResp = responses.stream().filter(r -> r.code().equals("ATTACK_BONUS")).findFirst().orElseThrow();
        assertTrue(attackResp.unlocked());
        assertFalse(attackResp.maxed());
        assertEquals(150, attackResp.nextCostHonorMarks());

        ClanUpgradeResponse defenseResp = responses.stream().filter(r -> r.code().equals("DEFENSE_BONUS")).findFirst().orElseThrow();
        assertFalse(defenseResp.unlocked());
    }
}
