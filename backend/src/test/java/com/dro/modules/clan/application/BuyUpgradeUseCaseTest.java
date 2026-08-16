package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanUpgradeResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanUpgradePurchase;
import com.dro.modules.clan.domain.ClanUpgradeType;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.ClanUpgradePurchaseRepository;
import com.dro.modules.clan.infra.ClanUpgradeTypeRepository;
import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyUpgradeUseCaseTest {

    @Mock private ClanRepository clanRepository;
    @Mock private ClanUpgradeTypeRepository upgradeTypeRepository;
    @Mock private ClanUpgradePurchaseRepository upgradePurchaseRepository;
    @Mock private ClanBonusService clanBonusService;
    @Mock private PlayerRepository playerRepository;

    @InjectMocks
    private BuyUpgradeUseCase useCase;

    private String makeToken(UUID playerId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    private Player makePlayer(UUID id, UUID clanId, ClanRole role) {
        return Player.builder()
                .id(id)
                .username("leader")
                .clanId(clanId)
                .clanRole(role)
                .build();
    }

    private Clan makeClan(UUID id, int level, int honorMarks) {
        return Clan.builder()
                .id(id)
                .name("Clan")
                .tag("TAG")
                .leaderId(UUID.randomUUID())
                .level(level)
                .maxMembers(5)
                .honorMarks(honorMarks)
                .build();
    }

    private ClanUpgradeType makeType() {
        return ClanUpgradeType.builder()
                .code("ATTACK_BONUS")
                .name("Attack")
                .unlockedAtClanLevel(1)
                .maxLevel(10)
                .baseHonorMarksCost(100)
                .costMultiplier(BigDecimal.valueOf(2.0))
                .effectPerLevel(BigDecimal.valueOf(0.01))
                .build();
    }

    @Test
    void execute_leaderBuysFirstLevel() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        String token = makeToken(playerId);
        Player player = makePlayer(playerId, clanId, ClanRole.LEADER);
        Clan clan = makeClan(clanId, 2, 150);
        ClanUpgradeType type = makeType();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));
        when(upgradeTypeRepository.findById("ATTACK_BONUS")).thenReturn(Optional.of(type));
        when(upgradePurchaseRepository.findByClanIdAndUpgradeCode(clanId, "ATTACK_BONUS"))
                .thenReturn(Optional.empty());
        when(clanBonusService.calculateNextCost(type, 0)).thenReturn(100);
        when(clanBonusService.listUpgrades(clanId)).thenReturn(List.of(
                new ClanUpgradeResponse("ATTACK_BONUS", "Attack", "", 1, 1, 10, 200,
                        BigDecimal.valueOf(0.01), 0.01, true, false)
        ));

        ClanUpgradeResponse response = useCase.execute(token, clanId, "ATTACK_BONUS");

        assertEquals("ATTACK_BONUS", response.code());
        assertEquals(1, response.currentLevel());
        assertEquals(50, clan.getHonorMarks());
        verify(upgradePurchaseRepository).save(any(ClanUpgradePurchase.class));
        verify(clanRepository).save(clan);
    }

    @Test
    void execute_throwsWhenNotLeader() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        String token = makeToken(playerId);
        Player player = makePlayer(playerId, clanId, ClanRole.MEMBER);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(ForbiddenException.class, () -> useCase.execute(token, clanId, "ATTACK_BONUS"));
    }
}
