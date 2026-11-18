package pe.andra.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DixonFechas {

    public static final String[] DIXON_MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
    public static final String[] DIXON_MESES_CORTOS = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep",
            "oct", "nov", "dic"};
    public static final String[] DIXON_DIAS = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};

    private static final DateTimeFormatter DIXON_DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIXON_HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DIXON_ISO_MINIMO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ROOT);

    private static Clock dixonReloj = Clock.systemDefaultZone();

    private DixonFechas() {
    }

    public static void dixonFijarReloj(Clock dixonNuevo) {
        dixonReloj = dixonNuevo == null ? Clock.systemDefaultZone() : dixonNuevo;
    }

    public static Clock dixonReloj() {
        return dixonReloj;
    }

    public static LocalDate dixonHoy() {
        return LocalDate.now(dixonReloj);
    }

    public static LocalDateTime dixonAhora() {
        return LocalDateTime.now(dixonReloj).withSecond(0).withNano(0);
    }

    public static long dixonMilis() {
        return dixonReloj.millis();
    }

    public static String dixonDmy(LocalDate dixonFecha) {
        return dixonFecha == null ? "" : dixonFecha.format(DIXON_DMY);
    }

    public static String dixonHm(LocalDateTime dixonMomento) {
        return dixonMomento == null ? "" : dixonMomento.format(DIXON_HM);
    }

    public static String dixonDmyHm(LocalDateTime dixonMomento) {
        return dixonMomento == null ? "" : dixonDmy(dixonMomento.toLocalDate()) + " " + dixonHm(dixonMomento);
    }

    public static String dixonCapitalizar(String dixonTexto) {
        return dixonTexto == null || dixonTexto.isEmpty() ? dixonTexto : Character.toUpperCase(dixonTexto.charAt(0)) + dixonTexto.substring(1);
    }

    public static String dixonDia(LocalDate dixonFecha) {
        return DIXON_DIAS[dixonFecha.getDayOfWeek().getValue() - 1];
    }

    public static String dixonLargo(LocalDate dixonFecha) {
        return dixonCapitalizar(dixonDia(dixonFecha)) + ", " + String.format("%02d", dixonFecha.getDayOfMonth()) + " de "
                + DIXON_MESES[dixonFecha.getMonthValue() - 1] + ", " + dixonFecha.getYear();
    }

    public static String dixonLargoSinDia(LocalDate dixonFecha) {
        return String.format("%02d", dixonFecha.getDayOfMonth()) + " de " + DIXON_MESES[dixonFecha.getMonthValue() - 1] + ", "
                + dixonFecha.getYear();
    }

    public static String dixonCorto(LocalDate dixonFecha) {
        return dixonCapitalizar(dixonDia(dixonFecha).substring(0, 3)) + " " + dixonFecha.getDayOfMonth() + " "
                + DIXON_MESES_CORTOS[dixonFecha.getMonthValue() - 1];
    }

    public static String dixonMesAnio(LocalDate dixonFecha) {
        return dixonCapitalizar(DIXON_MESES[dixonFecha.getMonthValue() - 1]) + " " + dixonFecha.getYear();
    }

    public static String dixonHora12(String dixonHhmm) {
        LocalTime dixonHora = LocalTime.parse(dixonHhmm);
        int dixonAlto = (dixonHora.getHour() + 11) % 12 + 1;
        return dixonAlto + ":" + String.format("%02d", dixonHora.getMinute()) + (dixonHora.getHour() >= 12 ? " p. m." : " a. m.");
    }

    public static String dixonIso(LocalDateTime dixonMomento) {
        return dixonMomento.format(DIXON_ISO_MINIMO);
    }

    public static LocalDateTime dixonLeerMomento(String dixonTexto) {
        if (dixonTexto == null || dixonTexto.isBlank()) {
            return null;
        }
        return dixonTexto.length() <= 10 ? LocalDate.parse(dixonTexto).atTime(9, 0) : LocalDateTime.parse(dixonTexto);
    }

    public static LocalDate dixonLeerFecha(String dixonTexto) {
        return dixonTexto == null || dixonTexto.isBlank() ? null : LocalDate.parse(dixonTexto.substring(0, 10));
    }

    public static LocalDate dixonDesdeTexto(String dixonTexto) {
        if (dixonTexto == null) {
            return null;
        }
        String dixonTexto2 = DixonTextoPlano.dixonNormalizar(dixonTexto);
        java.util.regex.Matcher dixonCoincidencia = java.util.regex.Pattern.compile("(\\d{1,2}) de (\\w+),? (\\d{4})").matcher(dixonTexto2);
        if (dixonCoincidencia.find()) {
            String dixonMes = dixonCoincidencia.group(2).equals("setiembre") ? "septiembre" : dixonCoincidencia.group(2);
            for (int dixonIndice = 0; dixonIndice < DIXON_MESES.length; dixonIndice++) {
                if (DixonTextoPlano.dixonNormalizar(DIXON_MESES[dixonIndice]).equals(dixonMes)) {
                    return LocalDate.of(Integer.parseInt(dixonCoincidencia.group(3)), dixonIndice + 1, Integer.parseInt(dixonCoincidencia.group(1)));
                }
            }
        }
        java.util.regex.Matcher dixonCoincidencia2 = java.util.regex.Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})").matcher(dixonTexto2);
        if (dixonCoincidencia2.find()) {
            return LocalDate.of(Integer.parseInt(dixonCoincidencia2.group(3)), Integer.parseInt(dixonCoincidencia2.group(2)), Integer.parseInt(dixonCoincidencia2.group(1)));
        }
        return null;
    }
}
