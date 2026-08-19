package com.dro.modules.tutorial.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Tutorial.
 */
public record TutorialProgressResponse(
        int completedSteps,
        int totalSteps,
        boolean allCompleted,
        List<TutorialStepResponse> steps
) {
}
