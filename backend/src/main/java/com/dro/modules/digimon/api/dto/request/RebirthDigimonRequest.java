package com.dro.modules.digimon.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record RebirthDigimonRequest(

        @NotNull
        UUID digimonId,

        @Min(0)
        Integer codeInfiniteHp,

        @Min(0)
        Integer codeInfiniteAttack,

        @Min(0)
        Integer codeInfiniteDefense,

        Boolean preserveRarity

) {
    public int codeInfiniteHpOrZero() {
        return codeInfiniteHp == null ? 0 : codeInfiniteHp;
    }

    public int codeInfiniteAttackOrZero() {
        return codeInfiniteAttack == null ? 0 : codeInfiniteAttack;
    }

    public int codeInfiniteDefenseOrZero() {
        return codeInfiniteDefense == null ? 0 : codeInfiniteDefense;
    }

    public boolean preserveRarityOrFalse() {
        return Boolean.TRUE.equals(preserveRarity);
    }
}
