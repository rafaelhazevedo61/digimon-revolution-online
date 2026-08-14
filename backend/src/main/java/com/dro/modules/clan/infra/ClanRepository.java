package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.Clan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClanRepository extends JpaRepository<Clan, UUID> {

    boolean existsByName(String name);

    boolean existsByTag(String tag);

    Optional<Clan> findByNameIgnoreCase(String name);

    Optional<Clan> findByTagIgnoreCase(String tag);

    @Query("SELECT c FROM Clan c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.tag) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Clan> searchByNameOrTag(@Param("query") String query, Pageable pageable);
}
