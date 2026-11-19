package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.inventory.domain.ItemType;

/**
 * Regras de progressão de estágios e requisitos de Digievolução.
 *
 * <p>Os nomes oficiais dos estágios não são traduzidos: Baby, Rookie, Champion,
 * Ultimate e Mega permanecem como valores de domínio. Cada transição possui um
 * nível mínimo, um fragmento opcional e um multiplicador próprio de atributos.</p>
 */
public class EvolutionRules {

    /** Retorna o próximo estágio oficial ou {@code null} quando não há transição. */
    public static Stage nextStage(Stage current) {
        return switch (current) {
            case BABY -> Stage.BABY_II;
            case BABY_II -> Stage.ROOKIE;
            case ROOKIE -> Stage.CHAMPION;
            case CHAMPION -> Stage.ULTIMATE;
            case ULTIMATE -> Stage.MEGA;
            default -> null;
        };
    }

    /** Retorna o nível mínimo exigido para evoluir a partir do estágio informado. */
    public static int requiredLevel(Stage current) {
        return switch (current) {
            case BABY -> 10;
            case BABY_II -> 15;
            case ROOKIE -> 25;
            case CHAMPION -> 50;
            case ULTIMATE -> 75;
            default -> Integer.MAX_VALUE;
        };
    }

    /** Retorna o tipo de fragmento exigido pela próxima evolução, quando houver. */
    public static ItemType requiredFragment(Stage current) {
        return switch (current) {
            case BABY_II -> ItemType.FRAGMENT_ROOKIE;
            case ROOKIE -> ItemType.FRAGMENT_CHAMPION;
            case CHAMPION -> ItemType.FRAGMENT_ULTIMATE;
            case ULTIMATE -> ItemType.FRAGMENT_MEGA;
            default -> null; // BABY não precisa
        };
    }

    /** Retorna a quantidade de fragmentos exigida pela próxima evolução. */
    public static int requiredFragmentQuantity(Stage current) {
        return switch (current) {
            case BABY_II -> 5;
            case ROOKIE -> 10;
            case CHAMPION -> 20;
            case ULTIMATE -> 50;
            default -> 0;
        };
    }

    /** Retorna o multiplicador de HP, ATK e DEF associado ao estágio. */
    public static double stageStatMultiplier(Stage stage) {
        return switch (stage) {
            case BABY -> 1.0;
            case BABY_II -> 1.1;
            case ROOKIE -> 1.2;
            case CHAMPION -> 1.5;
            case ULTIMATE -> 2.0;
            case MEGA -> 2.8;
        };
    }
}
