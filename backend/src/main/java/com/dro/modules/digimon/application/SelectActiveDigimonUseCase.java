package com.dro.modules.digimon.application;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Compatibilidade do fluxo de seleção com o caso de uso de ativação única.
 */
@Service
public class SelectActiveDigimonUseCase {
    private final ActivateDigimonUseCase activateDigimonUseCase;

    public void execute(String token, UUID digimonId) {
        activateDigimonUseCase.execute(token, digimonId);
    }

    public SelectActiveDigimonUseCase(final ActivateDigimonUseCase activateDigimonUseCase) {
        this.activateDigimonUseCase = activateDigimonUseCase;
    }
}
