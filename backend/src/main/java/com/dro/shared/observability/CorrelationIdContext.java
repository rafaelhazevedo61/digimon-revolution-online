package com.dro.shared.observability;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Mantém o correlation ID da requisição atual no MDC do thread.
 */
public final class CorrelationIdContext {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    private static final int MAX_LENGTH = 64;
    private static final Pattern SAFE_VALUE = Pattern.compile("[A-Za-z0-9._:-]{1,64}");

    private CorrelationIdContext() {
    }

    /** Retorna um ID recebido se ele for seguro ou cria um UUID novo. */
    public static String resolve(String candidate) {
        if (candidate != null && SAFE_VALUE.matcher(candidate).matches()) {
            return candidate;
        }
        return UUID.randomUUID().toString();
    }

    /** Coloca o ID no MDC para ser incluído pelos appenders de log. */
    public static void put(String correlationId) {
        MDC.put(MDC_KEY, correlationId);
    }

    /** Retorna o ID do contexto ou {@code null} fora de uma requisição. */
    public static String current() {
        return MDC.get(MDC_KEY);
    }

    /** Remove o ID para impedir vazamento entre requisições reutilizadas pelo servidor. */
    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
