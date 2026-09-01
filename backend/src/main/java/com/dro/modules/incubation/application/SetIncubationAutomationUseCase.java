package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class SetIncubationAutomationUseCase {
    private final IncubationRepository repository;
    public SetIncubationAutomationUseCase(IncubationRepository repository) { this.repository = repository; }

    @Transactional
    public void execute(String token, UUID id, boolean autoClaim, Boolean autoRepeat) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Incubation incubation = repository.findByIdAndPlayerIdForUpdate(id, playerId)
                .orElseThrow(() -> new NotFoundException("Incubation not found"));
        if (incubation.getStatus() == IncubationStatus.CLAIMED) throw new BadRequestException("Incubation already claimed");
        incubation.setAutoClaimEnabled(autoClaim);
        if (autoRepeat != null) incubation.setAutoRepeatEnabled(autoRepeat);
        if (!autoClaim) incubation.setAutoRepeatEnabled(false);
        if (incubation.isAutoRepeatEnabled()) incubation.setAutoClaimEnabled(true);
        repository.save(incubation);
    }
}
