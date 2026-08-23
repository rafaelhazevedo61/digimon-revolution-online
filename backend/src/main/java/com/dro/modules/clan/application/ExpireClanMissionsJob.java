package com.dro.modules.clan.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mantém o ciclo diário das missões de clã,
 * expirando missões não concluídas de dias anteriores.
 */
@Service
public class ExpireClanMissionsJob {

    private static final Logger log =
            LoggerFactory.getLogger(ExpireClanMissionsJob.class);

    private final ExpireClanMissionsUseCase expireClanMissionsUseCase;

    @Value("${dro.clan.missions.time-zone:America/Sao_Paulo}")
    private String timeZone;

    public ExpireClanMissionsJob (ExpireClanMissionsUseCase expireClanMissionsUseCase) {
        this.expireClanMissionsUseCase = expireClanMissionsUseCase;
    }

    @Scheduled(
            cron = "${dro.clan.missions.expiration-cron:0 0 0 * * *}",
            zone = "${dro.clan.missions.time-zone:America/Sao_Paulo}"
    )
    public void expirePreviousDayMissions() {
        expireOldMissions();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void expirePreviousDayMissionsOnStartup() {
        expireOldMissions();
    }

    private void expireOldMissions() {
        ZoneId zoneId = ZoneId.of(timeZone);

        LocalDateTime cutoff = LocalDateTime
                .now(zoneId)
                .toLocalDate()
                .atStartOfDay();

        int expired = expireClanMissionsUseCase.execute(cutoff);

        if (expired > 0) {
            log.info(
                    "Expired {} unfinished clan mission(s) accepted before {} ({})",
                    expired,
                    cutoff,
                    zoneId
            );
        }
    }
}