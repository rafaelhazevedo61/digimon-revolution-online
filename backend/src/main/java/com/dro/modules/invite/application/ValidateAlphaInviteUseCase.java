package com.dro.modules.invite.application;

import com.dro.modules.invite.domain.AlphaInvite;
import com.dro.modules.invite.infra.AlphaInviteRepository;
import com.dro.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ValidateAlphaInviteUseCase {
    private static final String INVALID_INVITE_MESSAGE = "Código de convite inválido, expirado ou já utilizado.";
    private final AlphaInviteRepository repository;
    private final AlphaInviteCodeService codeService;

    public AlphaInvite lockValidInvite(String rawCode, String registrationEmail) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new BadRequestException("Código de convite da Alpha é obrigatório.");
        }
        AlphaInvite invite = repository.findByCodeHashForUpdate(codeService.hash(rawCode)).orElseThrow(() -> new BadRequestException(INVALID_INVITE_MESSAGE));
        LocalDateTime now = LocalDateTime.now();
        if (invite.isDeleted()) {
            throw new IllegalArgumentException("Código de convite inválido, expirado ou já utilizado.");
        }
        if (invite.isUsed() || invite.isExpired(now)) {
            throw new BadRequestException(INVALID_INVITE_MESSAGE);
        }
        if (!invite.getTesterEmail().equalsIgnoreCase(registrationEmail.trim())) {
            throw new BadRequestException("O e-mail informado não corresponde ao tester associado a este convite.");
        }
        return invite;
    }

    public ValidateAlphaInviteUseCase(final AlphaInviteRepository repository, final AlphaInviteCodeService codeService) {
        this.repository = repository;
        this.codeService = codeService;
    }
}
