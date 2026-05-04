package com.dro.modules.inventory.api.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record ItemDefinitionPageResponse(
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        List<ItemDefinitionResponse> items
) {
    public static ItemDefinitionPageResponse from(Page<ItemDefinitionResponse> pageResult) {
        return new ItemDefinitionPageResponse(
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