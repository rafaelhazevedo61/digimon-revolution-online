package com.dro.modules.player.infra;

import com.dro.modules.player.domain.PlayerDisplayPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerDisplayPreferenceRepository extends JpaRepository<PlayerDisplayPreference, UUID> {
}
