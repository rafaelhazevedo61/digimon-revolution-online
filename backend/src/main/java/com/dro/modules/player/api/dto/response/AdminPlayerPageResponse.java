package com.dro.modules.player.api.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record AdminPlayerPageResponse(
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        List<AdminPlayerResponse> items
) {
    public static AdminPlayerPageResponse from(Page<AdminPlayerResponse> pageResult) {
        return new AdminPlayerPageResponse(
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