package com.dro.modules.invite.infra;

import com.dro.modules.invite.domain.AlphaInvite;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlphaInviteRepository extends JpaRepository<AlphaInvite, UUID> {

    boolean existsByCodeHash(String codeHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM AlphaInvite i WHERE i.codeHash = :codeHash")
    Optional<AlphaInvite> findByCodeHashForUpdate(@Param("codeHash") String codeHash);

    List<AlphaInvite> findTop100ByOrderByCreatedAtDesc();
}
