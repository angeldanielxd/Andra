package pe.andra.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class AndraDiasHabiles {

    private AndraDiasHabiles() {
    }

    public static boolean andraEsHabil(LocalDate andraFecha) {
        DayOfWeek andraDiaSemana = andraFecha.getDayOfWeek();
        return andraDiaSemana != DayOfWeek.SATURDAY && andraDiaSemana != DayOfWeek.SUNDAY;
    }

    public static LocalDate andraSumar(LocalDate andraDesde, int andraDias) {
        LocalDate andraFecha = andraDesde;
        int andraRestantes = andraDias;
        while (andraRestantes > 0) {
            andraFecha = andraFecha.plusDays(1);
            if (andraEsHabil(andraFecha)) {
                andraRestantes--;
            }
        }
        return andraFecha;
    }
}
