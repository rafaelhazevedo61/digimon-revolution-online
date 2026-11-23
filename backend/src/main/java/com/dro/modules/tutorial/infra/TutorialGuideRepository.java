package com.dro.modules.tutorial.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Repository
public class TutorialGuideRepository {
    private final JdbcTemplate jdbc;
    public TutorialGuideRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Set<String> readTopics(UUID playerId) {
        return new HashSet<>(jdbc.query("SELECT topic FROM trainer_guide_read_topics WHERE player_id = ?",
                (rs, row) -> rs.getString("topic"), playerId));
    }
    public void acknowledge(UUID playerId, String topic) {
        jdbc.update("""
                INSERT INTO trainer_guide_read_topics (player_id, topic) VALUES (?, ?)
                ON CONFLICT (player_id, topic) DO NOTHING
                """, playerId, topic);
    }
}
