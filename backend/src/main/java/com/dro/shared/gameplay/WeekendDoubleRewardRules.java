package com.dro.shared.gameplay;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Regra temporal do evento recorrente de XP e Bits em dobro.
 *
 * <p>A janela é calculada no fuso de São Paulo/Brasília, sem depender de um
 * job em background: cada concessão de recompensa consulta o horário atual.
 * O evento começa às sextas-feiras às 19:00 e termina na virada para
 * segunda-feira, após o domingo às 23:59:59.</p>
 */
public final class WeekendDoubleRewardRules {
    public static final ZoneId EVENT_ZONE = ZoneId.of("America/Sao_Paulo");
    public static final LocalTime START_TIME = LocalTime.of(19, 0);
    public static final int XP_MULTIPLIER = 2;
    public static final int BITS_MULTIPLIER = 2;

    public static boolean isActive(Instant instant) {
        return isActive(instant.atZone(EVENT_ZONE));
    }

    public static boolean isActive(ZonedDateTime dateTime) {
        ZonedDateTime local = dateTime.withZoneSameInstant(EVENT_ZONE);
        DayOfWeek day = local.getDayOfWeek();
        LocalTime time = local.toLocalTime();
        if (day == DayOfWeek.FRIDAY) return !time.isBefore(START_TIME);
        if (day == DayOfWeek.SATURDAY) return true;
        return day == DayOfWeek.SUNDAY;
    }

    public static int multiplyXp(int baseXp, Instant instant) {
        return multiply(baseXp, XP_MULTIPLIER, instant);
    }

    public static int multiplyBits(int baseBits, Instant instant) {
        return multiply(baseBits, BITS_MULTIPLIER, instant);
    }

    private static int multiply(int value, int multiplier, Instant instant) {
        if (value <= 0 || !isActive(instant)) return value;
        return Math.toIntExact(Math.min(Integer.MAX_VALUE, (long) value * multiplier));
    }

    private WeekendDoubleRewardRules() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
