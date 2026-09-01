package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class SetIncubationAutomationUseCase {
    private final IncubationRepository repository;
    private final PlayerRepository playerRepository;
    public SetIncubationAutomationUseCase(IncubationRepository repository, PlayerRepository playerRepository) { this.repository = repository; this.playerRepository = playerRepository; }

    @Transactional
    public void execute(String token, UUID id, boolean autoClaim, Boolean autoRepeat) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        Incubation incubation = repository.findByIdAndPlayerIdForUpdate(id, playerId)
                .orElseThrow(() -> new NotFoundException("Incubation not found"));
        if (incubation.getStatus() == IncubationStatus.CLAIMED) throw new BadRequestException("Incubation already claimed");
        incubation.setAutoClaimEnabled(autoClaim);
        if (autoRepeat != null) incubation.setAutoRepeatEnabled(autoRepeat);
        if (autoClaim || (autoRepeat != null && autoRepeat)) incubation.clearAutomationPause();
        if (!autoClaim) incubation.setAutoRepeatEnabled(false);
        if (incubation.isAutoRepeatEnabled()) incubation.setAutoClaimEnabled(true);
        player.setIncubationAutoClaimEnabled(autoClaim);
        player.setIncubationAutoRepeatEnabled(autoRepeat != null && autoRepeat);
        repository.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED).forEach(active -> {
            active.setAutoClaimEnabled(player.isIncubationAutoClaimEnabled());
            active.setAutoRepeatEnabled(player.isIncubationAutoRepeatEnabled());
            if (player.isIncubationAutoClaimEnabled() || player.isIncubationAutoRepeatEnabled()) active.clearAutomationPause();
            repository.save(active);
        });
        playerRepository.save(player);
    }
}
