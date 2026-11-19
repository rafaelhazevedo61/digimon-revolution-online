package com.dro.shared.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditSchedulingConfigurationTest {

    @Test
    void auditObjectMapper_returnsConfiguredMapper() throws Exception {
        ObjectMapper objectMapper = new AuditSchedulingConfiguration().auditObjectMapper();

        assertThat(objectMapper).isNotNull();
        assertThat(objectMapper.writeValueAsString(java.util.Map.of("summary", "outbox-test")))
                .contains("outbox-test");
    }
}
