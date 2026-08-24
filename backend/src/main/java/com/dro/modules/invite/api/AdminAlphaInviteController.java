package com.dro.modules.invite.api;

import com.dro.modules.invite.api.dto.AlphaInviteResponse;
import com.dro.modules.invite.api.dto.CreateAlphaInviteRequest;
import com.dro.modules.invite.api.dto.CreateAlphaInviteResponse;
import com.dro.modules.invite.application.CreateAlphaInviteUseCase;
import com.dro.modules.invite.application.DeleteAlphaInviteUseCase;
import com.dro.modules.invite.application.ListAlphaInvitesUseCase;
import com.dro.shared.audit.AdminAuditService;
import com.dro.shared.util.TokenExtractor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/alpha-invites")
public class AdminAlphaInviteController {

    private final CreateAlphaInviteUseCase createUseCase;
    private final ListAlphaInvitesUseCase listUseCase;
    private final AdminAuditService adminAuditService;
    private final DeleteAlphaInviteUseCase deleteUseCase;

    public AdminAlphaInviteController (CreateAlphaInviteUseCase createUseCase, ListAlphaInvitesUseCase listUseCase, AdminAuditService adminAuditService, DeleteAlphaInviteUseCase deleteUseCase) {
        this.createUseCase = createUseCase;
        this.listUseCase = listUseCase;
        this.adminAuditService = adminAuditService;
        this.deleteUseCase = deleteUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateAlphaInviteResponse> create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid CreateAlphaInviteRequest request
    ) {
        UUID adminId = TokenExtractor.extractPlayerId(authorization);
        CreateAlphaInviteResponse response = createUseCase.execute(request, adminId);

        adminAuditService.success(
                authorization,
                "ADMIN_CREATE_ALPHA_INVITE",
                "AlphaInvite",
                response.id().toString(),
                "create-alpha-invite",
                "Convite de Alpha criado",
                Map.of(
                        "testerName", response.testerName(),
                        "testerEmail", response.testerEmail(),
                        "expiresAt", response.expiresAt().toString()
                )
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AlphaInviteResponse>> list() {
        return ResponseEntity.ok(listUseCase.execute());
    }

    @DeleteMapping("/{inviteId}")
    public ResponseEntity<Void> delete(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID inviteId
    ) {

        UUID adminId = TokenExtractor.extractPlayerId(authorization);

        deleteUseCase.execute(inviteId, adminId);

        adminAuditService.success(
                authorization,
                "ADMIN_DELETE_ALPHA_INVITE",
                "AlphaInvite",
                inviteId.toString(),
                "delete-alpha-invite",
                "Convite de Alpha removido logicamente",
                Map.of()
        );

        return ResponseEntity.noContent().build();
    }
}
