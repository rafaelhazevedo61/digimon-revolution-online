package com.dro.modules.mail.api;

import com.dro.modules.mail.api.dto.request.MailActionRequest;
import com.dro.modules.mail.api.dto.request.SendMailMessageRequest;
import com.dro.modules.mail.api.dto.response.MailActionResponse;
import com.dro.modules.mail.api.dto.response.MailMessagePageResponse;
import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.api.dto.response.MarkAllMailReadResponse;
import com.dro.modules.mail.application.DeleteMailMessageUseCase;
import com.dro.modules.mail.application.GetMailMessageUseCase;
import com.dro.modules.mail.application.GetUnreadMailCountUseCase;
import com.dro.modules.mail.application.ListMailMessagesUseCase;
import com.dro.modules.mail.application.MarkMailReadUseCase;
import com.dro.modules.mail.application.MarkAllMailReadUseCase;
import com.dro.modules.mail.application.ProcessMailActionUseCase;
import com.dro.modules.mail.application.SendMailMessageUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints autenticados para consultar e operar o Correio do jogador.
 *
 * <p>Todas as rotas usam o jogador identificado pelo token JWT. A autorização
 * de cada mensagem é revalidada pelos use cases antes de qualquer leitura,
 * exclusão ou ação.</p>
 */
@RestController
@RequestMapping("/mail")
public class MailController {
    private final ListMailMessagesUseCase listMailMessagesUseCase;
    private final SendMailMessageUseCase sendMailMessageUseCase;
    private final GetMailMessageUseCase getMailMessageUseCase;
    private final MarkMailReadUseCase markMailReadUseCase;
    private final MarkAllMailReadUseCase markAllMailReadUseCase;
    private final DeleteMailMessageUseCase deleteMailMessageUseCase;
    private final GetUnreadMailCountUseCase getUnreadMailCountUseCase;
    private final ProcessMailActionUseCase processMailActionUseCase;

    /**
     * Lista a Entrada do jogador, paginada pela data de criação mais recente.
     *
     * @param authorization token JWT do jogador
     * @param page índice da página, começando em zero
     * @param size quantidade solicitada de mensagens, limitada pelas regras do Correio
     * @return página de mensagens recebidas
     */
    @GetMapping("/inbox")
    public ResponseEntity<MailMessagePageResponse> inbox(@RequestHeader("Authorization") String authorization, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listMailMessagesUseCase.inbox(authorization, page, size));
    }

    /**
     * Lista as mensagens enviadas pelo jogador, sem expor mensagens excluídas por ele.
     *
     * @param authorization token JWT do jogador
     * @param page índice da página, começando em zero
     * @param size quantidade solicitada de mensagens
     * @return página de mensagens enviadas
     */
    @GetMapping("/sent")
    public ResponseEntity<MailMessagePageResponse> sent(@RequestHeader("Authorization") String authorization, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listMailMessagesUseCase.sent(authorization, page, size));
    }

    /**
     * Retorna a quantidade de mensagens não lidas na Entrada.
     *
     * @param authorization token JWT do jogador
     * @return objeto contendo a contagem de mensagens não lidas
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(Map.of("count", getUnreadMailCountUseCase.execute(authorization)));
    }

    /**
     * Busca uma mensagem visível para o jogador autenticado.
     *
     * @param authorization token JWT do jogador
     * @param messageId identificador da mensagem
     * @return mensagem completa
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<MailMessageResponse> get(@RequestHeader("Authorization") String authorization, @PathVariable UUID messageId) {
        return ResponseEntity.ok(getMailMessageUseCase.execute(authorization, messageId));
    }

    /**
     * Envia uma mensagem de texto para outro jogador.
     *
     * <p>O use case valida existência do destinatário, impede envio para si
     * mesmo, aplica limites de texto e rate limit e cria cópias independentes
     * para remetente e destinatário.</p>
     *
     * @param authorization token JWT do remetente
     * @param request assunto, corpo e username do destinatário
     * @return mensagem criada
     */
    @PostMapping
    public ResponseEntity<MailMessageResponse> send(@RequestHeader("Authorization") String authorization, @RequestBody @Valid SendMailMessageRequest request) {
        return ResponseEntity.ok(sendMailMessageUseCase.execute(authorization, request));
    }

    /**
     * Marca uma mensagem recebida como lida.
     *
     * @param authorization token JWT do destinatário
     * @param messageId identificador da mensagem
     * @return mensagem após a atualização do estado de leitura
     */
    @PostMapping("/{messageId}/read")
    public ResponseEntity<MailMessageResponse> markRead(@RequestHeader("Authorization") String authorization, @PathVariable UUID messageId) {
        return ResponseEntity.ok(markMailReadUseCase.execute(authorization, messageId));
    }

    /**
     * Marca como lidas todas as mensagens recebidas, exceto mensagens de
     * premiação de evento com loot pendente para resgate.
     *
     * @param authorization token JWT do destinatário
     * @return quantidade de mensagens marcadas como lidas
     */
    @PostMapping("/read-all")
    public ResponseEntity<MarkAllMailReadResponse> markAllRead(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(markAllMailReadUseCase.execute(authorization));
    }

    /**
     * Processa uma ação especial da mensagem, como resgatar prêmio ou responder a convite.
     *
     * @param authorization token JWT do jogador destinatário
     * @param messageId identificador da mensagem com ação pendente
     * @param request ação solicitada
     * @return resultado da ação
     */
    @PostMapping("/{messageId}/action")
    public ResponseEntity<MailActionResponse> action(@RequestHeader("Authorization") String authorization, @PathVariable UUID messageId, @RequestBody @Valid MailActionRequest request) {
        return ResponseEntity.ok(processMailActionUseCase.execute(authorization, messageId, request.action()));
    }

    /**
     * Exclui a cópia da mensagem pertencente ao jogador autenticado.
     *
     * <p>A cópia do outro participante permanece independente.</p>
     *
     * @param authorization token JWT do jogador
     * @param messageId identificador da mensagem
     * @return resposta sem conteúdo quando a exclusão é concluída
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String authorization, @PathVariable UUID messageId) {
        deleteMailMessageUseCase.execute(authorization, messageId);
        return ResponseEntity.noContent().build();
    }

    public MailController(final ListMailMessagesUseCase listMailMessagesUseCase, final SendMailMessageUseCase sendMailMessageUseCase, final GetMailMessageUseCase getMailMessageUseCase, final MarkMailReadUseCase markMailReadUseCase, final MarkAllMailReadUseCase markAllMailReadUseCase, final DeleteMailMessageUseCase deleteMailMessageUseCase, final GetUnreadMailCountUseCase getUnreadMailCountUseCase, final ProcessMailActionUseCase processMailActionUseCase) {
        this.listMailMessagesUseCase = listMailMessagesUseCase;
        this.sendMailMessageUseCase = sendMailMessageUseCase;
        this.getMailMessageUseCase = getMailMessageUseCase;
        this.markMailReadUseCase = markMailReadUseCase;
        this.markAllMailReadUseCase = markAllMailReadUseCase;
        this.deleteMailMessageUseCase = deleteMailMessageUseCase;
        this.getUnreadMailCountUseCase = getUnreadMailCountUseCase;
        this.processMailActionUseCase = processMailActionUseCase;
    }
}
