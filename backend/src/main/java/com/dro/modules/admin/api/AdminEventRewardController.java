package com.dro.modules.admin.api;

import com.dro.modules.admin.api.dto.AdminEventRewardRequest;
import com.dro.modules.event.application.CreateEventRewardUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints administrativos para criação de premiações de eventos pelo Correio.
 *
 * <p>As rotas delegam a validação de autorização, destinatários, conteúdo e
 * idempotência ao caso de uso. Somente usuários com tipo {@code ADMIN} podem
 * executar a operação.</p>
 */
@RestController
@RequestMapping("/admin/mail")
public class AdminEventRewardController {

    private final CreateEventRewardUseCase createEventRewardUseCase;

    public AdminEventRewardController (CreateEventRewardUseCase createEventRewardUseCase) {
        this.createEventRewardUseCase = createEventRewardUseCase;
    }

    /**
     * Cria premiações individuais para os destinatários selecionados.
     *
     * @param request conteúdo, validade e estratégia de destinatários
     * @return contagem de criadas, ignoradas e IDs das premiações envolvidas
     */
    @PostMapping("/event-rewards")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody @Valid AdminEventRewardRequest request
    ) {
        var result = createEventRewardUseCase.execute(request);
        return ResponseEntity.ok(Map.of(
                "message", "Premiação processada para os destinatários.",
                "requestedCount", result.requestedCount(),
                "createdCount", result.createdCount(),
                "skippedCount", result.skippedCount(),
                "skippedUsernames", result.skippedUsernames(),
                "rewardIds", result.rewardIds()
        ));
    }
}
