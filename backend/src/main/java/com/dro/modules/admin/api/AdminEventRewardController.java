package com.dro.modules.admin.api;

import com.dro.modules.admin.api.dto.AdminEventRewardRequest;
import com.dro.modules.event.application.CreateEventRewardUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/mail")
@RequiredArgsConstructor
public class AdminEventRewardController {

    private final CreateEventRewardUseCase createEventRewardUseCase;

    @PostMapping("/event-rewards")
    public ResponseEntity<Map<String, Object>> create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid AdminEventRewardRequest request
    ) {
        var reward = createEventRewardUseCase.execute(authorization, request);
        return ResponseEntity.ok(Map.of(
                "message", "Premiação criada e enviada pelo Correio.",
                "rewardId", reward.getId(),
                "playerId", reward.getPlayer().getId(),
                "expiresAt", reward.getExpiresAt()
        ));
    }
}
