package com.dro.modules.clan.raid.infra;

import com.dro.modules.clan.raid.domain.ClanRaidReward;
import com.dro.modules.clan.raid.domain.ClanRaidRewardType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistência das concessões de Baús das incursões de clã.
 */
public interface ClanRaidRewardRepository extends JpaRepository<ClanRaidReward, UUID> {
    Optional<ClanRaidReward> findByEventKey(String eventKey);

    @EntityGraph(attributePaths = {"chestDefinition"})
    List<ClanRaidReward> findBySourceAttackIdOrderByRewardTypeAsc(UUID sourceAttackId);

    @EntityGraph(attributePaths = {"chestDefinition"})
    List<ClanRaidReward> findByClanRaidIdAndRecipientPlayerIdOrderByCreatedAtAsc(
            UUID clanRaidId,
            UUID recipientPlayerId
    );

    boolean existsByClanRaidIdAndRewardType(UUID clanRaidId, ClanRaidRewardType rewardType);
}
