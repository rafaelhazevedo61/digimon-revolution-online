package com.dro.modules.clan.raid.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidRepository;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Componente da camada de serviço de aplicação do módulo de Clãs.
 */
@Service
public class ClanRaidService {
    private static final Duration RESPAWN_DELAY = Duration.ofHours(1);
    private final ClanRaidRepository clanRaidRepository;
    private final BossDefinitionRepository bossDefinitionRepository;
    private final GameplayConfig gameplayConfig;

    @Transactional
    public ClanRaid getOrCreateToday(UUID clanId) {
        Instant startOfDay = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant();
        ClanRaid current = clanRaidRepository.findFirstByClanIdOrderByCreatedAtDesc(clanId).orElse(null);
        boolean respawnReady = current != null
                && current.getStatus() == ClanRaidStatus.DEFEATED
                && current.getDefeatedAt() != null
                && !Instant.now().isBefore(current.getDefeatedAt().plus(RESPAWN_DELAY));
        if (current == null || (gameplayConfig.isAutoBossRespawnAfterDefeatEnabled() && respawnReady)) {
            return createNewRaid(clanId);
        }
        return current;
    }

    private ClanRaid createNewRaid(UUID clanId) {
        BossDefinitionEntity boss = bossDefinitionRepository.findAllActive().stream().filter(b -> b.getBossType() == BossType.CLAN).min((a, b) -> {
            int stageCompare = Integer.compare(a.getRequiredStage().ordinal(), b.getRequiredStage().ordinal());
            if (stageCompare != 0) return stageCompare;
            return Integer.compare(a.getRequiredLevel(), b.getRequiredLevel());
        }).orElseThrow(() -> new NotFoundException("No clan raid boss is available"));
        Instant now = Instant.now();
        ClanRaid raid = ClanRaid.builder().id(UUID.randomUUID()).clanId(clanId).bossId(boss.getId()).maxHp(boss.getHp()).remainingHp(boss.getHp()).status(ClanRaidStatus.ACTIVE).createdAt(now).updatedAt(now).dailyResetAt(now).build();
        return clanRaidRepository.save(raid);
    }

    public ClanRaidService(final ClanRaidRepository clanRaidRepository, final BossDefinitionRepository bossDefinitionRepository, final GameplayConfig gameplayConfig) {
        this.clanRaidRepository = clanRaidRepository;
        this.bossDefinitionRepository = bossDefinitionRepository;
        this.gameplayConfig = gameplayConfig;
    }
}
