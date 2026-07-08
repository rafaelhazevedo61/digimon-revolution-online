package com.dro.modules.tutorial.api.dto.response;

public record TutorialStepResponse(
        String step,
        int order,
        String title,
        String description,
        int rewardBits,
        String rewardItem,
        int rewardItemQuantity,
        boolean completed
) {
}
