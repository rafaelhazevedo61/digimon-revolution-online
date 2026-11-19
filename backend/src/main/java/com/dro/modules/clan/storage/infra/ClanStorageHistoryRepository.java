package com.dro.modules.clan.storage.infra;

import com.dro.modules.clan.storage.domain.ClanStorageHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClanStorageHistoryRepository extends JpaRepository<ClanStorageHistory, UUID> {
    default List<ClanStorageHistory> findRecentByClanId(UUID clanId, int limit) {
        return findByClanIdOrderByCreatedAtDesc(clanId, PageRequest.of(0, limit));
    }

    List<ClanStorageHistory> findByClanIdOrderByCreatedAtDesc(UUID clanId, org.springframework.data.domain.Pageable pageable);
}
