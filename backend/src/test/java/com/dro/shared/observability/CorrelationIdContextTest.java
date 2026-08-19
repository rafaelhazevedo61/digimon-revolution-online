package com.dro.shared.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdContextTest {

    @AfterEach
    void clearContext() {
        CorrelationIdContext.clear();
    }

    @Test
    void resolve_keepsSafeIncomingValue() {
        assertThat(CorrelationIdContext.resolve("request-123"))
                .isEqualTo("request-123");
    }

    @Test
    void resolve_replacesUnsafeOrTooLongValue() {
        String resolved = CorrelationIdContext.resolve("header with spaces");

        assertThat(resolved).isNotEqualTo("header with spaces");
        assertThat(resolved).matches("[0-9a-f-]{36}");
    }

    @Test
    void putAndClear_manageMdcValue() {
        CorrelationIdContext.put("request-1");

        assertThat(CorrelationIdContext.current()).isEqualTo("request-1");

        CorrelationIdContext.clear();

        assertThat(CorrelationIdContext.current()).isNull();
    }
}
