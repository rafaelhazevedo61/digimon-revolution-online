package com.dro.modules.tower.api;

import com.dro.modules.tower.domain.TowerRules.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class TowerDtos {
    private TowerDtos() {}
    public record Challenge(@NotNull UUID teamId, @NotNull UUID requestId, @PositiveOrZero long expectedAttempts) {}
    public record Result(UUID requestId, long floor, boolean won, int rounds, String teamName, String enemyName, Reward reward) {}
    public record Page(long highestFloor, long attempts, Instant serverTime, Instant availableAt, Floor nextFloor, Result lastResult) {}
}
