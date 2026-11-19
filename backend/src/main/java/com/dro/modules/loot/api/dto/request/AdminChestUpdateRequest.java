package com.dro.modules.loot.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados administrativos editáveis de um Baú da Área.
 *
 * @param name nome exibido no painel e no jogo
 * @param description descrição administrativa do baú
 * @param icon ícone catalogado ou identificador visual
 * @param lootTableCode código da Loot Table ativa vinculada
 * @param tradable indica se o baú pode ser negociado
 * @param active indica se o baú pode ser entregue e aberto
 */
public record AdminChestUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 5000) String description,
        @Size(max = 120) String icon,
        @NotBlank @Size(max = 80) String lootTableCode,
        Boolean tradable,
        Boolean active
) {
}
