package com.dro.modules.boss.world.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.world.domain.WorldBossAttack;
import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossStatus;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.world.infra.WorldBossAttackRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.config.GameplayConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldBossResponseMapperTest {

    @Mock
    private BossDefinitionRepository bossDefinitionRepository;

    @Mock
    private WorldBossAttackRepository worldBossAttackRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private WorldBossRewardService worldBossRewardService;

    @Mock
    private GameplayConfig gameplayConfig;

    @InjectMocks
    private WorldBossResponseMapper mapper;

    @Test
    void doesNotExposeNextAttackWhenCooldownIsDisabled() {
        UUID worldBossId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        WorldBossInstance instance = WorldBossInstance.builder()
                .id(worldBossId)
                .bossId(1L)
                .bossDate(LocalDate.now())
                .maxHp(1_000_000)
                .remainingHp(900_000)
                .status(WorldBossStatus.ACTIVE)
                .createdAt(Instant.now().minusSeconds(3600))
                .build();
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .id(1L)
                .code("WORLD_BOSS_APOCALYMON")
                .name("Apocalymon")
                .cooldownMinutes(5)
                .cooldownEnabled(false)
                .build();
        WorldBossAttack previousAttack = WorldBossAttack.builder()
                .id(UUID.randomUUID())
                .worldBossId(worldBossId)
                .playerId(playerId)
                .digimonId(UUID.randomUUID())
                .damage(100)
                .createdAt(Instant.now().minusSeconds(60))
                .build();

        when(bossDefinitionRepository.findById(1L)).thenReturn(Optional.of(boss));
        when(worldBossAttackRepository.countByWorldBossIdAndPlayerIdAndCreatedAtGreaterThanEqual(
                any(), any(), any())).thenReturn(1L);
        when(worldBossAttackRepository.findByWorldBossIdAndPlayerIdOrderByCreatedAtDesc(worldBossId, playerId))
                .thenReturn(List.of(previousAttack));
        when(worldBossAttackRepository.findByWorldBossIdOrderByCreatedAtDesc(worldBossId))
                .thenReturn(List.of(previousAttack));
        when(playerRepository.findAllById(any())).thenReturn(List.of());
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());
        when(worldBossRewardService.findPlayerRewards(worldBossId, playerId)).thenReturn(List.of());
        when(gameplayConfig.getWorldBossDailyAttackLimit()).thenReturn(10);

        var response = mapper.toResponse(instance, playerId);

        assertThat(response.cooldownEnabled()).isFalse();
        assertThat(response.attackCooldownMinutes()).isEqualTo(5);
        assertThat(response.nextAttackAvailableAt()).isNull();
        assertThat(response.dailyAttacksRemaining()).isEqualTo(9);
    }
}
