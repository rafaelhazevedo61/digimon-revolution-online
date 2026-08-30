package com.dro.modules.activitycalendar.infra;

import com.dro.modules.activitycalendar.domain.ActivityPointEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ActivityPointEventRepository extends JpaRepository<ActivityPointEvent, UUID> {
    boolean existsByPlayerIdAndSourceAndSourceReferenceId(UUID playerId, String source, String sourceReferenceId);

    @Query(value = """
            SELECT TO_CHAR(activity_date, 'YYYY-MM') AS yearMonth,
                   COALESCE(SUM(points), 0) AS points,
                   COUNT(DISTINCT player_id) AS activePlayers,
                   COUNT(*) AS events
            FROM activity_point_events
            WHERE activity_date >= :fromDate
            GROUP BY TO_CHAR(activity_date, 'YYYY-MM')
            ORDER BY yearMonth
            """, nativeQuery = true)
    List<ActivityMonthlyAggregateProjection> aggregateMonthlySince(@Param("fromDate") LocalDate fromDate);
}
