package com.dro.modules.admin.api;

import com.dro.modules.admin.api.dto.AdminAnnouncementRequest;
import com.dro.modules.admin.application.CreateAdminAnnouncementUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Componente da camada de controller da API do módulo de Administração.
 */
@RestController
@RequestMapping("/admin/mail")
public class AdminMailController {
    private final CreateAdminAnnouncementUseCase createAdminAnnouncementUseCase;

    @PostMapping("/announcements")
    public ResponseEntity<Map<String, Object>> createAnnouncement(@RequestBody @Valid AdminAnnouncementRequest request) {
        int delivered = createAdminAnnouncementUseCase.execute(request);
        return ResponseEntity.ok(Map.of("message", "Comunicado enviado pelo Correio.", "delivered", delivered));
    }

    public AdminMailController(final CreateAdminAnnouncementUseCase createAdminAnnouncementUseCase) {
        this.createAdminAnnouncementUseCase = createAdminAnnouncementUseCase;
    }
}
