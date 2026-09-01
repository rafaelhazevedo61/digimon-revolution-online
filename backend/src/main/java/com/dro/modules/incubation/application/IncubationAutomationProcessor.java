package com.dro.modules.incubation.application;

import com.dro.modules.digimon.application.StoreDigimonUseCase;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class IncubationAutomationProcessor {
    private final IncubationRepository repository;
    private final ClaimIncubationUseCase claimUseCase;
    private final StoreDigimonUseCase storeDigimonUseCase;
    private final StartIncubationUseCase startUseCase;

    public IncubationAutomationProcessor(
            IncubationRepository repository,
            ClaimIncubationUseCase claimUseCase,
            StoreDigimonUseCase storeDigimonUseCase,
            StartIncubationUseCase startUseCase
    ) {
        this.repository = repository;
        this.claimUseCase = claimUseCase;
        this.storeDigimonUseCase = storeDigimonUseCase;
        this.startUseCase = startUseCase;
    }

    /**
     * Coleta a incubação, armazena o Digimon e, somente depois, inicia a repetição.
     * Todas as operações participam desta transação REQUIRES_NEW. Assim, se o
     * armazém estiver cheio ou o novo ciclo não puder ser iniciado, o claim e o
     * consumo dos itens do novo ciclo também são revertidos.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(UUID id) {
        Incubation i = repository.findByIdForUpdate(id).orElse(null);
        if (i == null || !i.isAutoClaimEnabled()
                || (i.getStatus() != IncubationStatus.IN_PROGRESS && i.getStatus() != IncubationStatus.READY)
                || i.getFinishAt().isAfter(LocalDateTime.now())) {
            return;
        }

        Digimon digimon = claimUseCase.executeForPlayer(i.getPlayerId(), id);
        storeDigimonUseCase.executeForPlayer(i.getPlayerId(), digimon.getId());

        if (i.isAutoRepeatEnabled()) {
            startUseCase.executeForPlayer(
                    i.getPlayerId(),
                    i.getSlotNumber(),
                    i.getDigitamaType(),
                    i.getIncubatorType(),
                    true,
                    true
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void pause(UUID id) {
        repository.findByIdForUpdate(id).ifPresent(i -> {
            i.setAutoClaimEnabled(false);
            i.setAutoRepeatEnabled(false);
            repository.save(i);
        });
    }
}
