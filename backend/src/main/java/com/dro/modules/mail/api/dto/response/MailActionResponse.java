package com.dro.modules.mail.api.dto.response;

public record MailActionResponse(
        boolean completed,
        String message
) {
}
