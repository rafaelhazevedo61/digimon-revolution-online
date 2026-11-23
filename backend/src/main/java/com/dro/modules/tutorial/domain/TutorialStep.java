package com.dro.modules.tutorial.domain;

import com.dro.modules.inventory.domain.ItemType;

/**
 * Enumera estados, tipos ou classificações usados pelo módulo de Tutorial.
 */
public enum TutorialStep {

    SELECT_DIGITAMA(
            1,
            "Escolha sua Digitama",
            "Escolha a Digitama do seu primeiro parceiro na tela inicial.",
            0,
            null,
            0
    ),
    HATCH_DIGIMON(
            2,
            "Choque seu Digimon",
            "Conclua o nascimento na tela inicial e conheça os atributos do seu primeiro parceiro.",
            100,
            null,
            0
    ),
    COMPLETE_MISSION(
            3,
            "Complete uma Missão",
            "Na tela Missões, escolha um desafio liberado, aguarde o término e resgate Bits, experiência e os itens obtidos.",
            100,
            ItemType.POTION_SMALL,
            3
    ),
    BUY_SHOP(
            4,
            "Compre na Loja",
            "Conheça os preços da Loja e faça sua primeira compra com Bits. Confira os limites e guarde recursos para evoluir.",
            0,
            ItemType.REFINEMENT_STONE,
            2
    ),
    EQUIP_ITEM(
            5,
            "Equipe um Item",
            "No Inventário, equipe uma arma, armadura ou acessório pertencente ao seu Digimon e confira os bônus.",
            150,
            null,
            0
    ),
    EVOLVE_DIGIMON(
            6,
            "Evolua seu Digimon",
            "Na tela Evolução, confira o nível e os fragmentos exigidos. Faça missões para reunir os requisitos e evolua para o próximo estágio.",
            300,
            null,
            0
    );

    private final int order;
    private final String title;
    private final String description;
    private final int rewardBits;
    private final ItemType rewardItem;
    private final int rewardItemQuantity;

    TutorialStep(
            int order,
            String title,
            String description,
            int rewardBits,
            ItemType rewardItem,
            int rewardItemQuantity
    ) {
        this.order = order;
        this.title = title;
        this.description = description;
        this.rewardBits = rewardBits;
        this.rewardItem = rewardItem;
        this.rewardItemQuantity = rewardItemQuantity;
    }

    public int getOrder() {
        return order;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getRewardBits() {
        return rewardBits;
    }

    public ItemType getRewardItem() {
        return rewardItem;
    }

    public int getRewardItemQuantity() {
        return rewardItemQuantity;
    }

    public boolean hasItemReward() {
        return rewardItem != null && rewardItemQuantity > 0;
    }

    public boolean hasReward() {
        return rewardBits > 0 || hasItemReward();
    }
}
