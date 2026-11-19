package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.Clan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Clãs.
 */
public interface ClanRepository extends JpaRepository<Clan, UUID> {

    /** Nome só precisa ser único entre clãs ativos: um clã dissolvido não bloqueia reuso do nome. */
    boolean existsByNameAndActiveTrue(String name);

    /** Sigla só precisa ser única entre clãs ativos: um clã dissolvido não bloqueia reuso da sigla. */
    boolean existsByTagAndActiveTrue(String tag);

    Optional<Clan> findByNameIgnoreCase(String name);

    Optional<Clan> findByTagIgnoreCase(String tag);

    List<Clan> findByActiveTrue();

    Page<Clan> findByActiveTrue(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Clan c WHERE c.id = :id")
    Optional<Clan> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT c FROM Clan c WHERE c.active = true AND (" +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.tag) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Clan> searchByNameOrTag(@Param("query") String query, Pageable pageable);
}