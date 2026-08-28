package com.dro.modules.mail.application;

import com.dro.modules.mail.api.dto.response.MarkAllMailReadResponse;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.util.TokenExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkAllMailReadUseCaseTest {

    @Mock
    private MailMessageRepository mailMessageRepository;

    @Test
    void marksEligibleMessagesAndReturnsUpdatedCount() {
        UUID playerId = UUID.randomUUID();
        String token = "Bearer token";
        when(mailMessageRepository.markAllEligibleAsRead(eq(playerId), any(LocalDateTime.class))).thenReturn(4);

        MarkAllMailReadResponse response;
        try (MockedStatic<TokenExtractor> tokenExtractor = mockStatic(TokenExtractor.class)) {
            tokenExtractor.when(() -> TokenExtractor.extractPlayerId(token)).thenReturn(playerId);
            response = new MarkAllMailReadUseCase(mailMessageRepository).execute(token);
        }

        assertEquals(4, response.markedCount());
        verify(mailMessageRepository).markAllEligibleAsRead(eq(playerId), any(LocalDateTime.class));
    }
}
