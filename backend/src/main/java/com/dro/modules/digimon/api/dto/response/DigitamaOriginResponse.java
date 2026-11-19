package com.dro.modules.digimon.api.dto.response;

/**
 * Digi Egg/pool capaz de gerar um Digimon Baby.
 *
 * @param code código estável da pool
 * @param name nome exibível da Digi Egg
 */
public record DigitamaOriginResponse(
        String code,
        String name
) {
}
