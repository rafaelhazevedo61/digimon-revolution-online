package com.dro.modules.invite.application;

import com.dro.modules.invite.api.dto.AlphaInviteResponse;
import com.dro.modules.invite.domain.AlphaInvite;
import com.dro.modules.invite.infra.AlphaInviteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListAlphaInvitesUseCase {
    private final AlphaInviteRepository repository;

    @Transactional(readOnly = true)
    public List<AlphaInviteResponse> execute() {
        LocalDateTime now = LocalDateTime.now();
        return repository.findTop100ByOrderByCreatedAtDesc().stream().map(invite -> toResponse(invite, now)).toList();
    }

    private AlphaInviteResponse toResponse(AlphaInvite invite, LocalDateTime now) {
        return new AlphaInviteResponse(invite.getId(), invite.getCodeHint(), invite.getTesterName(), invite.getTesterEmail(), resolveStatus(invite, now), invite.getCreatedAt(), invite.getExpiresAt(), invite.getUsedAt(), invite.getUsedByPlayerId(), invite.getCreatedByAdminId(), invite.getDeletedAt(), invite.getDeletedByAdminId());
    }

    private String resolveStatus(AlphaInvite invite, LocalDateTime now) {
        if (invite.isDeleted()) {
            return "DELETED";
        }
        if (invite.isUsed()) {
            return "USED";
        }
        if (invite.isExpired(now)) {
            return "EXPIRED";
        }
        return "AVAILABLE";
    }

    public ListAlphaInvitesUseCase(final AlphaInviteRepository repository) {
        this.repository = repository;
    }
}
