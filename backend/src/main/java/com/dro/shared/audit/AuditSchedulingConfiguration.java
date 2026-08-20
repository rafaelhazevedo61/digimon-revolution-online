package com.dro.shared.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita a publicação periódica dos eventos do Transactional Outbox.
 */
@Configuration
@EnableScheduling
public class AuditSchedulingConfiguration {

    /** Fornece o serializador usado pelo Outbox para payloads de auditoria. */
    @Bean
    public ObjectMapper auditObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
