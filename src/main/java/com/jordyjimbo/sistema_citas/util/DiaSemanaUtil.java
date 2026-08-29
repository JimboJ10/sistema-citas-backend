package com.jordyjimbo.sistema_citas.util;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

public class DiaSemanaUtil {

    private DiaSemanaUtil() {
    }

    /**
     * Convierte un DayOfWeek (interno, en inglés) a su nombre traducido.
     * Ej: DayOfWeek.MONDAY + Locale ES -> "lunes"
     *     DayOfWeek.MONDAY + Locale US -> "Monday"
     */
    public static String aTextoLocalizado(DayOfWeek dia, Locale locale) {
        return dia.getDisplayName(TextStyle.FULL, locale);
    }

    /**
     * Convierte un texto en español (ej: "lunes") de vuelta a DayOfWeek.
     * Necesario para cuando el cliente envía el día en español en un POST.
     */
    public static DayOfWeek desdeEspanol(String texto) {
        String normalizado = texto.trim().toUpperCase(Locale.ROOT);
        return switch (normalizado) {
            case "LUNES" -> DayOfWeek.MONDAY;
            case "MARTES" -> DayOfWeek.TUESDAY;
            case "MIERCOLES", "MIÉRCOLES" -> DayOfWeek.WEDNESDAY;
            case "JUEVES" -> DayOfWeek.THURSDAY;
            case "VIERNES" -> DayOfWeek.FRIDAY;
            case "SABADO", "SÁBADO" -> DayOfWeek.SATURDAY;
            case "DOMINGO" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Día de la semana no reconocido: " + texto);
        };
    }
}