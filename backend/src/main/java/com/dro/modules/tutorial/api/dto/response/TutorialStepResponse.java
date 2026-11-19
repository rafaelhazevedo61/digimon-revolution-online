package com.dro.modules.tutorial.api.dto.response;

/**
 * Contrato de dados do módulo de Tutorial.
 */
public record TutorialStepResponse(
        String step,
        int order,
        String title,
        String description,
        int rewardBits,
        String rewardItem,
        String rewardItemName,
        int rewardItemQuantity,
        boolean completed,
        boolean rewardClaimed
) {
}
