package com.dro.modules.mission.application;

import com.dro.modules.mission.api.dto.response.MissionSlotsResponse;
import com.dro.modules.mission.domain.MissionSlotRules;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Retorna a capacidade de slots e as missões que ocupam essa capacidade.
 */
@Service
public class GetMissionSlotsUseCase {
    private final PlayerRepository playerRepository;
    private final GetActiveMissionsUseCase getActiveMissionsUseCase;

    public MissionSlotsResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        int unlockedSlots = MissionSlotRules.normalizeUnlockedSlots(player.getUnlockedMissionSlots());
        return new MissionSlotsResponse(
                MissionSlotRules.TOTAL_SLOTS,
                unlockedSlots,
                getActiveMissionsUseCase.execute(token)
        );
    }

    public GetMissionSlotsUseCase(
            PlayerRepository playerRepository,
            GetActiveMissionsUseCase getActiveMissionsUseCase
    ) {
        this.playerRepository = playerRepository;
        this.getActiveMissionsUseCase = getActiveMissionsUseCase;
    }
}
