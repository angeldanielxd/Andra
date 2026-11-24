package pe.andra.util;

import pe.andra.model.AndraMetodoPago;

import java.time.YearMonth;

public final class AndraTarjetaJemm {

    private AndraTarjetaJemm() {
    }

    public static AndraMetodoPago andraMarca(String andraNumero) {
        String andraTexto = AndraValidacion.andraSoloDigitos(andraNumero);
        if (andraTexto.matches("^4.*")) {
            return AndraMetodoPago.VISA;
        }
        if (andraTexto.matches("^(5[1-5]|2[2-7]).*")) {
            return AndraMetodoPago.MASTERCARD;
        }
        if (andraTexto.matches("^3[47].*")) {
            return AndraMetodoPago.AMEX;
        }
        if (andraTexto.matches("^3(0[0-5]|[68]).*")) {
            return AndraMetodoPago.DINERS;
        }
        return null;
    }

    public static boolean andraLuhn(String andraNumero) {
        String andraTexto = AndraValidacion.andraSoloDigitos(andraNumero);
        if (andraTexto.length() < 13) {
            return false;
        }
        int andraSuma = 0;
        for (int andraIndice = 0; andraIndice < andraTexto.length(); andraIndice++) {
            int andraCantidad = andraTexto.charAt(andraTexto.length() - 1 - andraIndice) - '0';
            if (andraIndice % 2 == 1) {
                andraCantidad *= 2;
                if (andraCantidad > 9) {
                    andraCantidad -= 9;
                }
            }
            andraSuma += andraCantidad;
        }
        return andraSuma % 10 == 0;
    }

    public static String andraFormatear(String andraNumero) {
        String andraTexto = AndraValidacion.andraSoloDigitos(andraNumero);
        if (andraTexto.length() > 19) {
            andraTexto = andraTexto.substring(0, 19);
        }
        return andraTexto.replaceAll("(.{4})", "$1 ").trim();
    }

    public static String andraFormatearVencimiento(String andraTexto) {
        String andraTexto2 = AndraValidacion.andraSoloDigitos(andraTexto);
        if (andraTexto2.length() > 4) {
            andraTexto2 = andraTexto2.substring(0, 4);
        }
        return andraTexto2.length() > 2 ? andraTexto2.substring(0, 2) + "/" + andraTexto2.substring(2) : andraTexto2;
    }

    public static boolean andraVencimientoValido(String andraMmaa, YearMonth andraActual) {
        if (andraMmaa == null || !andraMmaa.matches("\\d{2}/\\d{2}")) {
            return false;
        }
        int andraMes = Integer.parseInt(andraMmaa.substring(0, 2));
        int andraAnio = 2000 + Integer.parseInt(andraMmaa.substring(3));
        if (andraMes < 1 || andraMes > 12) {
            return false;
        }
        return !YearMonth.of(andraAnio, andraMes).isBefore(andraActual);
    }
}
