package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Compatibilidade do endpoint de Storage com o caso de uso de ativação única.
 */
@Service
public class RetrieveDigimonUseCase {
    private final ActivateDigimonUseCase activateDigimonUseCase;

    public Digimon execute(String token, UUID digimonId) {
        return activateDigimonUseCase.executeStored(token, digimonId);
    }

    public RetrieveDigimonUseCase(final ActivateDigimonUseCase activateDigimonUseCase) {
        this.activateDigimonUseCase = activateDigimonUseCase;
    }
}
