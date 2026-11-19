package com.dro.modules.server.application;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Componente da camada de serviço de aplicação do módulo de Servidor.
 */
@Service
public class GlobalDamageBuffService {

    private static final double DEFAULT_MULTIPLIER = 100.0;

    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicReference<Double> multiplier = new AtomicReference<>(DEFAULT_MULTIPLIER);

    public boolean isEnabled() {
        return enabled.get();
    }

    public double getMultiplier() {
        return enabled.get() ? multiplier.get() : 1.0;
    }

    public double getConfiguredMultiplier() {
        return multiplier.get();
    }

    public State getState() {
        return new State(enabled.get(), multiplier.get());
    }

    public State toggle() {
        enabled.set(!enabled.get());
        return getState();
    }

    public State set(boolean enabled, double multiplier) {
        this.enabled.set(enabled);
        this.multiplier.set(Math.max(1.0, multiplier));
        return getState();
    }

    public State setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        return getState();
    }

    public record State(boolean enabled, double multiplier) {
    }
}
