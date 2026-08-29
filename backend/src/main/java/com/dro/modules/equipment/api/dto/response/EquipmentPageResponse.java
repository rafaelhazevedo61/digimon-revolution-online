package com.dro.modules.equipment.api.dto.response;

import java.util.List;

public record EquipmentPageResponse(
        List<EquipmentResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static EquipmentPageResponse of(List<EquipmentResponse> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new EquipmentPageResponse(content, page, size, totalElements, totalPages);
    }
}

