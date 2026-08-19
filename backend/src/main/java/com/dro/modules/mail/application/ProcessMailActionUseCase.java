package com.dro.modules.mail.application;

import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanInvitation;
import com.dro.modules.clan.domain.ClanInvitationStatus;
import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.clan.infra.ClanInvitationRepository;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.event.domain.EventReward;
import com.dro.modules.event.application.EventRewardMessageText;
import com.dro.modules.event.domain.EventRewardStatus;
import com.dro.modules.event.infra.EventRewardRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mail.api.dto.response.MailActionResponse;
import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.infra.MailMessageRepository;
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

/**
 * Processa ações disponíveis em mensagens especiais do Correio.
 *
 * <p>O destinatário autenticado é revalidado antes de qualquer alteração. As
 * ações de convite de clã e resgate de premiação são transacionais e atualizam
 * simultaneamente a entidade de origem, a mensagem e os recursos do jogador.</p>
 */
@Service
@RequiredArgsConstructor
public class ProcessMailActionUseCase {

    private final MailMessageRepository mailMessageRepository;
    private final ClanInvitationRepository clanInvitationRepository;
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanBonusService clanBonusService;
    private final EventRewardRepository eventRewardRepository;
    private final DigimonRepository digimonRepository;
    private final AddItemUseCase addItemUseCase;

    /**
     * Executa a ação solicitada pelo destinatário de uma mensagem.
     *
     * @param token token JWT do jogador autenticado
     * @param messageId mensagem que contém a ação
     * @param requestedAction ação informada pelo cliente, como {@code CLAIM},
     *                       {@code ACCEPT} ou {@code DECLINE}
     * @return resultado da ação e mensagem apropriada para o jogador
     * @throws NotFoundException quando a mensagem não pertence à caixa visível
     * @throws BadRequestException quando a ação não é compatível com a mensagem
     */
    @Transactional
    public MailActionResponse execute(String token, UUID messageId, String requestedAction) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        MailMessage message = mailMessageRepository.findVisibleById(messageId, playerId)
                .orElseThrow(() -> new NotFoundException("Mensagem não encontrada."));
        if (!message.belongsToRecipient(playerId)) {
            throw new BadRequestException("Somente o destinatário pode executar esta ação.");
        }

        if ("EVENT_REWARD".equals(message.getSourceType())
                && "EVENT_REWARD_CLAIM".equals(message.getActionType())
                && message.getSourceId() != null) {
            return claimEventReward(message, playerId, requestedAction);
        }

        if (!"CLAN_INVITATION".equals(message.getSourceType())
                || !"CLAN_INVITE".equals(message.getActionType())
                || message.getSourceId() == null) {
            throw new BadRequestException("Esta mensagem não possui uma ação disponível.");
        }
        return processClanInvitation(message, playerId, requestedAction);
    }

    /**
     * Revalida e processa um convite de clã.
     *
     * <p>A aceitação verifica novamente se o jogador continua sem clã, se o
     * convite está pendente e se ainda existe vaga. Uma falha de capacidade
     * mantém o convite pendente para nova tentativa.</p>
     */
    private MailActionResponse processClanInvitation(
            MailMessage message,
            UUID playerId,
            String requestedAction
    ) {
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
            completeMessage(message, now);
            return new MailActionResponse(false, "Este convite não está mais disponível.");
        }

        if ("DECLINE".equals(action)) {
            invitation.setStatus(ClanInvitationStatus.DECLINED);
            invitation.setActedAt(now);
            clanInvitationRepository.save(invitation);
            completeMessage(message, now);
            return new MailActionResponse(true, "Convite recusado.");
        }

        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
        Clan clan = clanRepository.findByIdForUpdate(invitation.getClan().getId())
                .orElseThrow(() -> new NotFoundException("Clã não encontrado."));

        if (player.getClanId() != null) {
            invitation.setStatus(ClanInvitationStatus.CANCELLED);
            invitation.setActedAt(now);
            clanInvitationRepository.save(invitation);
            completeMessage(message, now);
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
        clanInvitationRepository.save(invitation);
        completeMessage(message, now);
        return new MailActionResponse(true, "Você entrou no clã " + clan.getName() + ".");
    }

    /**
     * Resgata uma premiação de evento para o Digimon ativo do jogador.
     *
     * <p>A premiação é bloqueada pessimisticamente, a validade é revalidada e o
     * Digimon ativo é confirmado como pertencente ao jogador. Bits, item,
     * mensagem e status da premiação são atualizados na mesma transação. Se uma
     * pré-condição falhar, a premiação permanece disponível.</p>
     */
    private MailActionResponse claimEventReward(
            MailMessage message,
            UUID playerId,
            String requestedAction
    ) {
        if (!"CLAIM".equals(requestedAction == null ? "" : requestedAction.trim().toUpperCase())) {
            throw new BadRequestException("Ação de premiação inválida.");
        }

        UUID rewardId = message.getSourceId();
        EventReward reward = eventRewardRepository.findByIdAndPlayerIdForUpdate(rewardId, playerId)
                .orElseThrow(() -> new NotFoundException("Premiação não encontrada."));
        LocalDateTime now = LocalDateTime.now();
        if (!reward.isPendingAt(now)) {
            if (reward.getStatus() == EventRewardStatus.PENDING) {
                reward.setStatus(EventRewardStatus.EXPIRED);
                eventRewardRepository.save(reward);
            }
            completeMessage(message, now);
            return new MailActionResponse(false, "Esta premiação não está mais disponível.");
        }

        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
        if (player.getActiveDigimonId() == null) {
            throw new ConflictException("Selecione um Digimon ativo antes de resgatar a premiação.");
        }

        Digimon digimon = digimonRepository.findByIdForUpdate(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Digimon ativo não encontrado."));
        if (!playerId.equals(digimon.getPlayerId())) {
            throw new ConflictException("O Digimon ativo não pertence ao jogador atual.");
        }
        if ((long) digimon.getBits() + reward.getBitsAmount() > Integer.MAX_VALUE) {
            throw new ConflictException("O saldo de Bits do Digimon não comporta esta premiação.");
        }

        if (reward.getBitsAmount() > 0) {
            digimon.setBits(digimon.getBits() + reward.getBitsAmount());
        }
        if (reward.getItemQuantity() > 0) {
            addItemUseCase.execute(
                    digimon.getId(),
                    ItemType.valueOf(reward.getItemType()),
                    reward.getItemQuantity()
            );
        }
        digimonRepository.save(digimon);
        message.setBody(EventRewardMessageText.claimedBody(
                message.getBody(),
                reward.getBitsAmount(),
                reward.getItemType(),
                reward.getItemQuantity(),
                digimon.getName(),
                now
        ));

        reward.setStatus(EventRewardStatus.CLAIMED);
        reward.setClaimedAt(now);
        eventRewardRepository.save(reward);
        completeMessage(message, now);
        return new MailActionResponse(true, "Premiação resgatada com sucesso.");
    }

    /**
     * Finaliza uma mensagem cuja ação não deve ser executada novamente.
     *
     * @param message mensagem que será concluída
     * @param now instante da conclusão
     */
    private void completeMessage(MailMessage message, LocalDateTime now) {
        message.setActionType(null);
        message.setReadAt(now);
        mailMessageRepository.save(message);
    }
}
