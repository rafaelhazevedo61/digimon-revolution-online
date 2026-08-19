package com.dro.modules.mail.application;

import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanInvitation;
import com.dro.modules.clan.domain.ClanInvitationStatus;
import com.dro.modules.clan.infra.ClanInvitationRepository;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.mail.api.dto.response.MailActionResponse;
import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.modules.clan.domain.ClanRole;
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
public class ProcessMailActionUseCase {

    private final MailMessageRepository mailMessageRepository;
    private final ClanInvitationRepository clanInvitationRepository;
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanBonusService clanBonusService;

    @Transactional
    public MailActionResponse execute(String token, UUID messageId, String requestedAction) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        MailMessage message = mailMessageRepository.findVisibleById(messageId, playerId)
                .orElseThrow(() -> new NotFoundException("Mensagem não encontrada."));
        if (!message.belongsToRecipient(playerId)) {
            throw new BadRequestException("Somente o destinatário pode executar esta ação.");
        }
        if (!"CLAN_INVITATION".equals(message.getSourceType())
                || !"CLAN_INVITE".equals(message.getActionType())
                || message.getSourceId() == null) {
            throw new BadRequestException("Esta mensagem não possui uma ação disponível.");
        }

        String action = requestedAction == null ? "" : requestedAction.trim().toUpperCase();
        if (!"ACCEPT".equals(action) && !"DECLINE".equals(action)) {
            throw new BadRequestException("Ação de convite inválida.");
        }

        ClanInvitation invitation = clanInvitationRepository.findByIdForUpdate(message.getSourceId())
                .orElseThrow(() -> new NotFoundException("Convite não encontrado."));
        if (!invitation.getInvitee().getId().equals(playerId)) {
            throw new BadRequestException("Este convite não pertence ao jogador atual.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!invitation.isPendingAt(now)) {
            if (invitation.getStatus() == ClanInvitationStatus.PENDING) {
                invitation.setStatus(ClanInvitationStatus.EXPIRED);
                invitation.setActedAt(now);
                clanInvitationRepository.save(invitation);
            }
            message.setActionType(null);
            message.setReadAt(now);
            mailMessageRepository.save(message);
            return new MailActionResponse(false, "Este convite não está mais disponível.");
        }

        if ("DECLINE".equals(action)) {
            invitation.setStatus(ClanInvitationStatus.DECLINED);
            invitation.setActedAt(now);
            message.setActionType(null);
            message.setReadAt(now);
            clanInvitationRepository.save(invitation);
            mailMessageRepository.save(message);
            return new MailActionResponse(true, "Convite recusado.");
        }

        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
        Clan clan = clanRepository.findByIdForUpdate(invitation.getClan().getId())
                .orElseThrow(() -> new NotFoundException("Clã não encontrado."));

        if (player.getClanId() != null) {
            invitation.setStatus(ClanInvitationStatus.CANCELLED);
            invitation.setActedAt(now);
            message.setActionType(null);
            message.setReadAt(now);
            clanInvitationRepository.save(invitation);
            mailMessageRepository.save(message);
            return new MailActionResponse(false, "Você já pertence a um clã.");
        }

        long memberCount = playerRepository.countByClanId(clan.getId());
        if (memberCount >= clanBonusService.getEffectiveMaxMembers(clan)) {
            throw new ConflictException("O clã está cheio. O convite continua pendente.");
        }

        player.setClanId(clan.getId());
        player.setClanRole(ClanRole.MEMBER);
        player.setClanJoinedAt(now);
        playerRepository.save(player);

        invitation.setStatus(ClanInvitationStatus.ACCEPTED);
        invitation.setActedAt(now);
        message.setActionType(null);
        message.setReadAt(now);
        clanInvitationRepository.save(invitation);
        mailMessageRepository.save(message);
        return new MailActionResponse(true, "Você entrou no clã " + clan.getName() + ".");
    }
}
