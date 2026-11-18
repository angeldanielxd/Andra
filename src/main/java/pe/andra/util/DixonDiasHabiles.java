package pe.andra.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class DixonDiasHabiles {

    private DixonDiasHabiles() {
    }

    public static boolean dixonEsHabil(LocalDate dixonFecha) {
        DayOfWeek dixonDiaSemana = dixonFecha.getDayOfWeek();
        return dixonDiaSemana != DayOfWeek.SATURDAY && dixonDiaSemana != DayOfWeek.SUNDAY;
    }

    public static LocalDate dixonSumar(LocalDate dixonDesde, int dixonDias) {
        LocalDate dixonFecha = dixonDesde;
        int dixonRestantes = dixonDias;
        while (dixonRestantes > 0) {
            dixonFecha = dixonFecha.plusDays(1);
            if (dixonEsHabil(dixonFecha)) {
                dixonRestantes--;
            }
        }
        return dixonFecha;
    }
}
