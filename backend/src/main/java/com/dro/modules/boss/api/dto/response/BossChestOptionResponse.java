package com.dro.modules.boss.api.dto.response;

/**
 * Opção resumida de Baú disponível para a recompensa administrativa de um Boss.
 *
 * @param code código estável do Baú
 * @param name nome exibido no painel
 * @param lootTableCode código da Loot Table vinculada
 * @param lootTableName nome da Loot Table vinculada
 * @param active indica se o Baú está ativo
 * @param tradable indica se o Baú pode ser negociado
 */
public record BossChestOptionResponse(
        String code,
        String name,
        String lootTableCode,
        String lootTableName,
        boolean active,
        boolean tradable
) {
}
