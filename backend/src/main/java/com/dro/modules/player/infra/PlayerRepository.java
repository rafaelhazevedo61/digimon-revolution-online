package com.dro.modules.player.infra;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Jogadores.
 */
public interface PlayerRepository extends JpaRepository<Player, UUID>, JpaSpecificationExecutor<Player> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Player p WHERE p.id = :id")
    Optional<Player> findByIdForUpdate(@Param("id") UUID id);

    Optional<Player> findByEmail(String email);

    Optional<Player> findByUsername(String username);

    Optional<Player> findByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);

    List<Player> findByClanId(UUID clanId);

    List<Player> findByUserTypeOrderByUsernameAsc(UserType userType);

    List<Player> findTop100ByUsernameContainingIgnoreCaseOrderByUsernameAsc(String username);

    long countByClanId(UUID clanId);

    long countByUserType(UserType userType);

    boolean existsByUserType(UserType userType);
}