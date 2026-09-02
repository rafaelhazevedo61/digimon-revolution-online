package com.dro.shared.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AutomationMetrics {
    private final MeterRegistry registry;

    public AutomationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public Timer.Sample startRun(String mode) {
        counter("dro_automation_runs_total", "Total de itens processados pelos workers de automacao", mode).increment();
        return Timer.start(registry);
    }

    public void stopRun(String mode, Timer.Sample sample) {
        Timer timer = Timer.builder("dro_automation_cycle_duration")
                .description("Duracao de um ciclo individual de automacao")
                .tag("mode", normalize(mode))
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        sample.stop(timer);
    }

    public void recordFailure(String mode, String code) {
        Counter.builder("dro_automation_failures_total")
                .description("Falhas dos workers de automacao por codigo")
                .tag("mode", normalize(mode))
                .tag("code", normalize(code))
                .register(registry)
                .increment();
    }

    public void recordPause(String mode, String reason) {
        Counter.builder("dro_automation_pauses_total")
                .description("Pausas de automacao por motivo")
                .tag("mode", normalize(mode))
                .tag("reason", normalize(reason))
                .register(registry)
                .increment();
    }

    public void recordRetry(String mode) {
        counter("dro_automation_retries_total", "Retries de automacao", mode).increment();
    }

    public void recordSystemMail(String mode, String reason) {
        Counter.builder("dro_automation_system_mail_total")
                .description("Mensagens de sistema emitidas pelos workers")
                .tag("mode", normalize(mode))
                .tag("reason", normalize(reason))
                .register(registry)
                .increment();
    }

    private Counter counter(String name, String description, String mode) {
        return Counter.builder(name)
                .description(description)
                .tag("mode", normalize(mode))
                .register(registry);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "unknown" : value.toLowerCase(Locale.ROOT);
    }
}
