package com.dro.shared.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita a publicação periódica dos eventos do Transactional Outbox.
 */
@Configuration
@EnableScheduling
public class AuditSchedulingConfiguration {
}
