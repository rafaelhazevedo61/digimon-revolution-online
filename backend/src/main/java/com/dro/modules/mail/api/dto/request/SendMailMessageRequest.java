package com.dro.modules.mail.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMailMessageRequest(
        @NotBlank(message = "Recipient username is required")
        @Size(max = 30, message = "Recipient username must be at most 30 characters")
        String recipientUsername,

        @NotBlank(message = "Subject is required")
        @Size(max = 80, message = "Subject must be at most 80 characters")
        String subject,

        @NotBlank(message = "Message body is required")
        @Size(max = 1000, message = "Message body must be at most 1000 characters")
        String body
) {
}
