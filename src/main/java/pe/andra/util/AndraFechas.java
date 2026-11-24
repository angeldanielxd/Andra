package pe.andra.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class AndraFechas {

    public static final String[] ANDRA_MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
    public static final String[] ANDRA_MESES_CORTOS = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep",
            "oct", "nov", "dic"};
    public static final String[] ANDRA_DIAS = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};

    private static final DateTimeFormatter ANDRA_DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ANDRA_HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter ANDRA_ISO_MINIMO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ROOT);

    private static Clock andraReloj = Clock.systemDefaultZone();

    private AndraFechas() {
    }

    public static void andraFijarReloj(Clock andraNuevo) {
        andraReloj = andraNuevo == null ? Clock.systemDefaultZone() : andraNuevo;
    }

    public static Clock andraReloj() {
        return andraReloj;
    }

    public static LocalDate andraHoy() {
        return LocalDate.now(andraReloj);
    }

    public static LocalDateTime andraAhora() {
        return LocalDateTime.now(andraReloj).withSecond(0).withNano(0);
    }

    public static long andraMilis() {
        return andraReloj.millis();
    }

    public static String andraDmy(LocalDate andraFecha) {
        return andraFecha == null ? "" : andraFecha.format(ANDRA_DMY);
    }

    public static String andraHm(LocalDateTime andraMomento) {
        return andraMomento == null ? "" : andraMomento.format(ANDRA_HM);
    }

    public static String andraDmyHm(LocalDateTime andraMomento) {
        return andraMomento == null ? "" : andraDmy(andraMomento.toLocalDate()) + " " + andraHm(andraMomento);
    }

    public static String andraCapitalizar(String andraTexto) {
        return andraTexto == null || andraTexto.isEmpty() ? andraTexto : Character.toUpperCase(andraTexto.charAt(0)) + andraTexto.substring(1);
    }

    public static String andraDia(LocalDate andraFecha) {
        return ANDRA_DIAS[andraFecha.getDayOfWeek().getValue() - 1];
    }

    public static String andraLargo(LocalDate andraFecha) {
        return andraCapitalizar(andraDia(andraFecha)) + ", " + String.format("%02d", andraFecha.getDayOfMonth()) + " de "
                + ANDRA_MESES[andraFecha.getMonthValue() - 1] + ", " + andraFecha.getYear();
    }

    public static String andraLargoSinDia(LocalDate andraFecha) {
        return String.format("%02d", andraFecha.getDayOfMonth()) + " de " + ANDRA_MESES[andraFecha.getMonthValue() - 1] + ", "
                + andraFecha.getYear();
    }

    public static String andraCorto(LocalDate andraFecha) {
        return andraCapitalizar(andraDia(andraFecha).substring(0, 3)) + " " + andraFecha.getDayOfMonth() + " "
                + ANDRA_MESES_CORTOS[andraFecha.getMonthValue() - 1];
    }

    public static String andraMesAnio(LocalDate andraFecha) {
        return andraCapitalizar(ANDRA_MESES[andraFecha.getMonthValue() - 1]) + " " + andraFecha.getYear();
    }

    public static String andraHora12(String andraHhmm) {
        LocalTime andraHora = LocalTime.parse(andraHhmm);
        int andraAlto = (andraHora.getHour() + 11) % 12 + 1;
        return andraAlto + ":" + String.format("%02d", andraHora.getMinute()) + (andraHora.getHour() >= 12 ? " p. m." : " a. m.");
    }

    public static String andraIso(LocalDateTime andraMomento) {
        return andraMomento.format(ANDRA_ISO_MINIMO);
    }

    public static LocalDateTime andraLeerMomento(String andraTexto) {
        if (andraTexto == null || andraTexto.isBlank()) {
            return null;
        }
        return andraTexto.length() <= 10 ? LocalDate.parse(andraTexto).atTime(9, 0) : LocalDateTime.parse(andraTexto);
    }

    public static LocalDate andraLeerFecha(String andraTexto) {
        return andraTexto == null || andraTexto.isBlank() ? null : LocalDate.parse(andraTexto.substring(0, 10));
    }

    public static LocalDate andraDesdeTexto(String andraTexto) {
        if (andraTexto == null) {
            return null;
        }
        String andraTexto2 = AndraTextoPlano.andraNormalizar(andraTexto);
        java.util.regex.Matcher andraCoincidencia = java.util.regex.Pattern.compile("(\\d{1,2}) de (\\w+),? (\\d{4})").matcher(andraTexto2);
        if (andraCoincidencia.find()) {
            String andraMes = andraCoincidencia.group(2).equals("setiembre") ? "septiembre" : andraCoincidencia.group(2);
            for (int andraIndice = 0; andraIndice < ANDRA_MESES.length; andraIndice++) {
                if (AndraTextoPlano.andraNormalizar(ANDRA_MESES[andraIndice]).equals(andraMes)) {
                    return LocalDate.of(Integer.parseInt(andraCoincidencia.group(3)), andraIndice + 1, Integer.parseInt(andraCoincidencia.group(1)));
                }
            }
        }
        java.util.regex.Matcher andraCoincidencia2 = java.util.regex.Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})").matcher(andraTexto2);
        if (andraCoincidencia2.find()) {
            return LocalDate.of(Integer.parseInt(andraCoincidencia2.group(3)), Integer.parseInt(andraCoincidencia2.group(2)), Integer.parseInt(andraCoincidencia2.group(1)));
        }
        return null;
    }
}
