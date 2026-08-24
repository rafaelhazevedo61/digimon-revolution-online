package com.dro.modules.invite.application;

import com.dro.modules.invite.api.dto.CreateAlphaInviteRequest;
import com.dro.modules.invite.api.dto.CreateAlphaInviteResponse;
import com.dro.modules.invite.domain.AlphaInvite;
import com.dro.modules.invite.infra.AlphaInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateAlphaInviteUseCase {

    private final AlphaInviteRepository repository;
    private final AlphaInviteCodeService codeService;

    @Transactional
    public CreateAlphaInviteResponse execute(CreateAlphaInviteRequest request, UUID adminId) {
        String rawCode;
        String codeHash;
        do {
            rawCode = codeService.generate();
            codeHash = codeService.hash(rawCode);
        } while (repository.existsByCodeHash(codeHash));

        LocalDateTime now = LocalDateTime.now();
        AlphaInvite invite = new AlphaInvite(
                UUID.randomUUID(),
                codeHash,
                codeService.hint(rawCode),
                request.testerName().trim(),
                request.testerEmail().trim().toLowerCase(Locale.ROOT),
                now,
                request.expiresAt(),
                adminId
        );

        repository.save(invite);

        return new CreateAlphaInviteResponse(
                invite.getId(),
                rawCode,
                invite.getTesterName(),
                invite.getTesterEmail(),
                invite.getCreatedAt(),
                invite.getExpiresAt()
        );
    }
}
