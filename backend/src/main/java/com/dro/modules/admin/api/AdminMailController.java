package com.dro.modules.admin.api;

import com.dro.modules.admin.api.dto.AdminAnnouncementRequest;
import com.dro.modules.admin.application.CreateAdminAnnouncementUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/mail")
@RequiredArgsConstructor
public class AdminMailController {

    private final CreateAdminAnnouncementUseCase createAdminAnnouncementUseCase;

    @PostMapping("/announcements")
    public ResponseEntity<Map<String, Object>> createAnnouncement(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid AdminAnnouncementRequest request
    ) {
        int delivered = createAdminAnnouncementUseCase.execute(authorization, request);
        return ResponseEntity.ok(Map.of(
                "message", "Comunicado enviado pelo Correio.",
                "delivered", delivered
        ));
    }
}
