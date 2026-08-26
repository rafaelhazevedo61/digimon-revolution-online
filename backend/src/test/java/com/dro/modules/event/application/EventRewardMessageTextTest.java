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
    void multiItemMessageListsEachIndependentQuantity() {
        String body = EventRewardMessageText.pendingBody(
                "Pacote de evento.",
                0,
                java.util.List.of(
                        new EventRewardMessageText.ItemLabel("FRAGMENT_AGUMON", "Fragmento do Agumon", 3),
                        new EventRewardMessageText.ItemLabel("LOOT_CHEST", "Baú de teste", 2)
                ),
                LocalDateTime.of(2026, 8, 26, 18, 30)
        );

        assertTrue(body.contains("3 × Fragmento do Agumon"), body);
        assertTrue(body.contains("2 × Baú de teste"), body);
    }

    @Test
    void slotUnlockItemUsesLocalizedLabel() {
        String body = EventRewardMessageText.formatItem("INCUBATION_SLOT_UNLOCK", 1);

        assertTrue(body.contains("1 × Expansor de slot de incubação"), body);
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
