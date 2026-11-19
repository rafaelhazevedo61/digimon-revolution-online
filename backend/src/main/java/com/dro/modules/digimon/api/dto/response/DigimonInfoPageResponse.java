package com.dro.modules.digimon.api.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record DigimonInfoPageResponse(
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        List<DigimonInfoResponse> items
) {
    public static DigimonInfoPageResponse from(Page<DigimonInfoResponse> pageResult) {
        return new DigimonInfoPageResponse(
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