package com.dro.modules.mission.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

/**
 * Recompensa retornada ao concluir uma Missão.
 *
 * <p>As recompensas legadas continuam podendo informar apenas tipo e
 * quantidade. Recompensas catalogadas, como Baús da Área, também retornam
 * código e nome para o cliente localizar a definição correta no inventário.</p>
 */
public record RewardResponse(
        ItemType item,
        int quantity,
        String itemCode,
        String itemName
) {

    /**
     * Construtor de compatibilidade para recompensas não catalogadas.
     */
    public RewardResponse(ItemType item, int quantity) {
        this(item, quantity, null, null);
    }
}
