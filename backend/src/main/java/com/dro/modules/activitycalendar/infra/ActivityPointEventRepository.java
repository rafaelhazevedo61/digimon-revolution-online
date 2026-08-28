package com.dro.modules.activitycalendar.infra;

import com.dro.modules.activitycalendar.domain.ActivityPointEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ActivityPointEventRepository extends JpaRepository<ActivityPointEvent, UUID> {
    boolean existsByPlayerIdAndSourceAndSourceReferenceId(UUID playerId, String source, String sourceReferenceId);
}
