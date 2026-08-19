package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.request.ClanInviteRequest;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanInvitation;
import com.dro.modules.clan.domain.ClanInvitationStatus;
import com.dro.modules.clan.infra.ClanInvitationRepository;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.mail.domain.MailMessageMapper;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteClanPlayerUseCase {

    private static final int INVITATION_VALID_DAYS = 7;

    private final ClanRepository clanRepository;
    private final ClanInvitationRepository clanInvitationRepository;
    private final PlayerRepository playerRepository;
    private final ClanAuthorizationService clanAuthorizationService;
    private final ClanBonusService clanBonusService;
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    @Transactional
    public MailMessageResponse execute(String token, UUID clanId, ClanInviteRequest request) {
        UUID inviterId = TokenExtractor.extractPlayerId(token);
        Player inviter = clanAuthorizationService.getPlayer(inviterId);
        Clan clan = clanRepository.findByIdForUpdate(clanId)
                .orElseThrow(() -> new NotFoundException("Clã não encontrado."));
        clanAuthorizationService.assertCanInvite(inviter, clan);

        Player invitee = playerRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
        if (inviter.getId().equals(invitee.getId())) {
            throw new BadRequestException("Você não pode convidar a si mesmo.");
        }
        if (invitee.getClanId() != null) {
            throw new ConflictException("Este jogador já pertence a um clã.");
        }

        long memberCount = playerRepository.countByClanId(clan.getId());
        if (memberCount >= clanBonusService.getEffectiveMaxMembers(clan)) {
            throw new ConflictException("O clã está cheio.");
        }

        LocalDateTime now = LocalDateTime.now();
        clanInvitationRepository.findByClanIdAndInviteeIdAndStatus(
                        clan.getId(), invitee.getId(), ClanInvitationStatus.PENDING)
                .ifPresent(existing -> {
                    if (existing.isPendingAt(now)) {
                        throw new ConflictException("Já existe um convite pendente para este jogador.");
                    }
                    existing.setStatus(ClanInvitationStatus.EXPIRED);
                    existing.setActedAt(now);
                    clanInvitationRepository.saveAndFlush(existing);
                });

        ClanInvitation invitation = ClanInvitation.builder()
                .id(UUID.randomUUID())
                .clan(clan)
                .inviter(inviter)
                .invitee(invitee)
                .status(ClanInvitationStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusDays(INVITATION_VALID_DAYS))
                .build();
        clanInvitationRepository.save(invitation);

        String subject = "Convite para entrar no clã " + clan.getName();
        String body = "Você foi convidado por " + inviter.getUsername()
                + " para entrar no clã " + clan.getName() + " [" + clan.getTag() + "]."
                + " Este convite é válido por " + INVITATION_VALID_DAYS
                + " dias. Use as ações da mensagem para aceitar ou recusar.";
        var mail = createSystemMailMessageUseCase.create(
                MailMessageType.CLAN,
                "CLAN_INVITATION",
                invitee.getId(),
                invitation.getId(),
                "CLAN_INVITE",
                subject,
                body,
                "clan:invitation:" + invitation.getId()
        );
        return MailMessageMapper.toResponse(mail);
    }
}
