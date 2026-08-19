package com.dro.modules.mail.api;

import com.dro.modules.mail.api.dto.request.MailActionRequest;
import com.dro.modules.mail.api.dto.request.SendMailMessageRequest;
import com.dro.modules.mail.api.dto.response.MailActionResponse;
import com.dro.modules.mail.api.dto.response.MailMessagePageResponse;
import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.application.DeleteMailMessageUseCase;
import com.dro.modules.mail.application.GetMailMessageUseCase;
import com.dro.modules.mail.application.GetUnreadMailCountUseCase;
import com.dro.modules.mail.application.ListMailMessagesUseCase;
import com.dro.modules.mail.application.MarkMailReadUseCase;
import com.dro.modules.mail.application.ProcessMailActionUseCase;
import com.dro.modules.mail.application.SendMailMessageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

    private final ListMailMessagesUseCase listMailMessagesUseCase;
    private final SendMailMessageUseCase sendMailMessageUseCase;
    private final GetMailMessageUseCase getMailMessageUseCase;
    private final MarkMailReadUseCase markMailReadUseCase;
    private final DeleteMailMessageUseCase deleteMailMessageUseCase;
    private final GetUnreadMailCountUseCase getUnreadMailCountUseCase;
    private final ProcessMailActionUseCase processMailActionUseCase;

    @GetMapping("/inbox")
    public ResponseEntity<MailMessagePageResponse> inbox(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listMailMessagesUseCase.inbox(authorization, page, size));
    }

    @GetMapping("/sent")
    public ResponseEntity<MailMessagePageResponse> sent(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listMailMessagesUseCase.sent(authorization, page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(Map.of("count", getUnreadMailCountUseCase.execute(authorization)));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MailMessageResponse> get(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID messageId
    ) {
        return ResponseEntity.ok(getMailMessageUseCase.execute(authorization, messageId));
    }

    @PostMapping
    public ResponseEntity<MailMessageResponse> send(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SendMailMessageRequest request
    ) {
        return ResponseEntity.ok(sendMailMessageUseCase.execute(authorization, request));
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<MailMessageResponse> markRead(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID messageId
    ) {
        return ResponseEntity.ok(markMailReadUseCase.execute(authorization, messageId));
    }

    @PostMapping("/{messageId}/action")
    public ResponseEntity<MailActionResponse> action(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID messageId,
            @RequestBody @Valid MailActionRequest request
    ) {
        return ResponseEntity.ok(processMailActionUseCase.execute(
                authorization, messageId, request.action()));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID messageId
    ) {
        deleteMailMessageUseCase.execute(authorization, messageId);
        return ResponseEntity.noContent().build();
    }
}
