package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.ClanInvitation;
import com.dro.modules.clan.domain.ClanInvitationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Clãs.
 */
public interface ClanInvitationRepository extends JpaRepository<ClanInvitation, UUID> {

    boolean existsByClanIdAndInviteeIdAndStatus(
            UUID clanId,
            UUID inviteeId,
            ClanInvitationStatus status
    );

    Optional<ClanInvitation> findByClanIdAndInviteeIdAndStatus(
            UUID clanId,
            UUID inviteeId,
            ClanInvitationStatus status
    );

    Optional<ClanInvitation> findByIdAndInviteeId(UUID id, UUID inviteeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM ClanInvitation i WHERE i.id = :id")
    Optional<ClanInvitation> findByIdForUpdate(@Param("id") UUID id);
}
