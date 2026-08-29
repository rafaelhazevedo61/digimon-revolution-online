package com.dro.modules.inventory.api.dto.response;

import com.dro.modules.inventory.domain.InventoryItem;
import java.util.List;

public record InventoryPageResponse(
        List<InventoryItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static InventoryPageResponse of(List<InventoryItem> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new InventoryPageResponse(content, page, size, totalElements, totalPages);
    }
}

