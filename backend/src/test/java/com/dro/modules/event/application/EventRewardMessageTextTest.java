package com.dro.modules.event.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EventRewardMessageTextTest {

    @Test
    void pendingMessageExplainsWhatCanBeClaimed() {
        String body = EventRewardMessageText.pendingBody(
                "Obrigado por participar.",
                5000,
                "TRAINING_STONE",
                2,
                LocalDateTime.of(2026, 8, 26, 18, 30)
        );

        assertTrue(body.contains("5.000 Bits"), body);
        assertTrue(body.contains("2 × Pedra de treinamento"), body);
        assertTrue(body.contains("26/08/2026 18:30"), body);
        assertTrue(body.contains("Resgatar prêmio"), body);
    }

    @Test
    void claimedMessageRecordsWhatWasDelivered() {
        String body = EventRewardMessageText.claimedBody(
                "Prêmio disponível.",
                5000,
                "TRAINING_STONE",
                2,
                "Agumon",
                LocalDateTime.of(2026, 8, 19, 19, 10)
        );

        assertTrue(body.contains("5.000 Bits"), body);
        assertTrue(body.contains("2 × Pedra de treinamento"), body);
        assertTrue(body.contains("Digimon “Agumon”"), body);
        assertTrue(body.contains("19/08/2026 19:10"), body);
    }
}
