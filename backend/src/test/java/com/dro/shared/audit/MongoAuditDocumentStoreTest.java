package com.dro.shared.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoAuditDocumentStoreTest {

    @Mock
    private TransactionAuditRepository transactionAuditRepository;

    @Mock
    private ErrorLogRepository errorLogRepository;

    @InjectMocks
    private MongoAuditDocumentStore store;

    @Test
    void saveTransaction_routesDocumentToTransactionRepository() {
        TransactionAuditDocument document = transactionDocument();
        when(transactionAuditRepository.save(document)).thenReturn(document);

        TransactionAuditDocument saved = store.saveTransaction(document);

        assertThat(saved).isSameAs(document);
        verify(transactionAuditRepository).save(document);
    }

    @Test
    void saveError_routesDocumentToErrorRepository() {
        ErrorLogDocument document = errorDocument();
        when(errorLogRepository.save(document)).thenReturn(document);

        ErrorLogDocument saved = store.saveError(document);

        assertThat(saved).isSameAs(document);
        verify(errorLogRepository).save(document);
    }

    private static TransactionAuditDocument transactionDocument() {
        return new TransactionAuditDocument(
                null,
                "event-1",
                Instant.parse("2026-08-19T00:00:00Z"),
                "request-1",
                "player-1",
                "mail",
                "claimReward",
                "EventReward",
                "reward-1",
                "MAIL_REWARD_CLAIMED",
                AuditResult.SUCCESS,
                "Reward claimed",
                Map.of(),
                10L,
                TransactionAuditDocument.CURRENT_SCHEMA_VERSION
        );
    }

    private static ErrorLogDocument errorDocument() {
        return new ErrorLogDocument(
                null,
                "error-1",
                Instant.parse("2026-08-19T00:00:00Z"),
                "request-1",
                "player-1",
                "mail",
                "claimReward",
                "POST",
                "/mail/reward",
                409,
                "REWARD_ALREADY_CLAIMED",
                "ConflictException",
                "Reward is no longer available",
                "ConflictException: Reward is no longer available",
                TransactionOutcome.REJECTED,
                Map.of(),
                0,
                AuditSeverity.WARN,
                ErrorLogDocument.CURRENT_SCHEMA_VERSION
        );
    }
}
