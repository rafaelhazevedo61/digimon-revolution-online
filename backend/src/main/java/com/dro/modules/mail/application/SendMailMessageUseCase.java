package com.dro.modules.mail.application;

import com.dro.modules.mail.api.dto.request.SendMailMessageRequest;
import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.domain.MailMessageMapper;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.mail.domain.MailRules;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Envia mensagens de texto comuns entre jogadores.
 *
 * <p>O fluxo valida o destinatário, impede autoenvio, aplica o limite de
 * mensagens por minuto e persiste uma mensagem com cópias independentes para
 * remetente e destinatário.</p>
 */
@Service
public class SendMailMessageUseCase {
    private final PlayerRepository playerRepository;
    private final MailMessageRepository mailMessageRepository;

    /**
     * Envia uma mensagem para o jogador informado.
     *
     * @param token token JWT do remetente
     * @param request username, assunto e corpo da mensagem
     * @return representação da mensagem criada
     * @throws BadRequestException quando o destinatário não existe, é o próprio
     *                             remetente ou o texto está vazio
     * @throws UnprocessableException quando o rate limit foi atingido
     */
    @Transactional
    public MailMessageResponse execute(String token, SendMailMessageRequest request) {
        UUID senderId = TokenExtractor.extractPlayerId(token);
        Player sender = playerRepository.findById(senderId).orElseThrow(() -> new ConflictException("Não foi possível identificar o jogador remetente. Faça login novamente."));
        String recipientUsername = request.recipientUsername().trim();
        Player recipient = playerRepository.findByUsernameIgnoreCase(recipientUsername).orElseThrow(() -> new BadRequestException("Jogador destinatário não encontrado. Confira o nome informado e tente novamente."));
        if (sender.getId().equals(recipient.getId())) {
            throw new BadRequestException("Você não pode enviar uma mensagem para si mesmo.");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rateWindow = now.minusMinutes(1);
        if (mailMessageRepository.countBySenderIdAndCreatedAtAfter(senderId, rateWindow) >= MailRules.MAX_MESSAGES_PER_MINUTE) {
            throw new UnprocessableException("Você atingiu o limite de 10 mensagens por minuto. Aguarde um momento e tente novamente.");
        }
        String subject = request.subject().trim();
        String body = request.body().trim();
        if (subject.isEmpty() || body.isEmpty()) {
            throw new BadRequestException("O assunto e o texto da mensagem não podem ficar vazios.");
        }
        MailMessage message = MailMessage.builder().id(UUID.randomUUID()).sender(sender).recipient(recipient).messageType(MailMessageType.PLAYER).subject(subject).body(body).createdAt(now).build();
        return MailMessageMapper.toResponse(mailMessageRepository.save(message));
    }

    public SendMailMessageUseCase(final PlayerRepository playerRepository, final MailMessageRepository mailMessageRepository) {
        this.playerRepository = playerRepository;
        this.mailMessageRepository = mailMessageRepository;
    }
}
