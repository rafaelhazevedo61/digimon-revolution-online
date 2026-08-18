package com.dro.modules.mail.api.dto.response;

import java.util.List;

public record MailMessagePageResponse(
        List<MailMessageSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
