package com.dro.modules.evolution.api.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Contrato de dados do módulo de Evolução.
 */
public record EvolutionLinePageResponse(
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        List<EvolutionLineResponse> items
) {
    public static EvolutionLinePageResponse from(Page<EvolutionLineResponse> pageResult) {
        return new EvolutionLinePageResponse(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.hasNext(),
                pageResult.hasPrevious(),
                pageResult.getContent()
        );
    }
}