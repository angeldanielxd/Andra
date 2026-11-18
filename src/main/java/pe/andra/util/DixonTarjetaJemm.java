package pe.andra.util;

import pe.andra.model.DixonMetodoPago;

import java.time.YearMonth;

public final class DixonTarjetaJemm {

    private DixonTarjetaJemm() {
    }

    public static DixonMetodoPago dixonMarca(String dixonNumero) {
        String dixonTexto = DixonValidacion.dixonSoloDigitos(dixonNumero);
        if (dixonTexto.matches("^4.*")) {
            return DixonMetodoPago.VISA;
        }
        if (dixonTexto.matches("^(5[1-5]|2[2-7]).*")) {
            return DixonMetodoPago.MASTERCARD;
        }
        if (dixonTexto.matches("^3[47].*")) {
            return DixonMetodoPago.AMEX;
        }
        if (dixonTexto.matches("^3(0[0-5]|[68]).*")) {
            return DixonMetodoPago.DINERS;
        }
        return null;
    }

    public static boolean dixonLuhn(String dixonNumero) {
        String dixonTexto = DixonValidacion.dixonSoloDigitos(dixonNumero);
        if (dixonTexto.length() < 13) {
            return false;
        }
        int dixonSuma = 0;
        for (int dixonIndice = 0; dixonIndice < dixonTexto.length(); dixonIndice++) {
            int dixonCantidad = dixonTexto.charAt(dixonTexto.length() - 1 - dixonIndice) - '0';
            if (dixonIndice % 2 == 1) {
                dixonCantidad *= 2;
                if (dixonCantidad > 9) {
                    dixonCantidad -= 9;
                }
            }
            dixonSuma += dixonCantidad;
        }
        return dixonSuma % 10 == 0;
    }

    public static String dixonFormatear(String dixonNumero) {
        String dixonTexto = DixonValidacion.dixonSoloDigitos(dixonNumero);
        if (dixonTexto.length() > 19) {
            dixonTexto = dixonTexto.substring(0, 19);
        }
        return dixonTexto.replaceAll("(.{4})", "$1 ").trim();
    }

    public static String dixonFormatearVencimiento(String dixonTexto) {
        String dixonTexto2 = DixonValidacion.dixonSoloDigitos(dixonTexto);
        if (dixonTexto2.length() > 4) {
            dixonTexto2 = dixonTexto2.substring(0, 4);
        }
        return dixonTexto2.length() > 2 ? dixonTexto2.substring(0, 2) + "/" + dixonTexto2.substring(2) : dixonTexto2;
    }

    public static boolean dixonVencimientoValido(String dixonMmaa, YearMonth dixonActual) {
        if (dixonMmaa == null || !dixonMmaa.matches("\\d{2}/\\d{2}")) {
            return false;
        }
        int dixonMes = Integer.parseInt(dixonMmaa.substring(0, 2));
        int dixonAnio = 2000 + Integer.parseInt(dixonMmaa.substring(3));
        if (dixonMes < 1 || dixonMes > 12) {
            return false;
        }
        return !YearMonth.of(dixonAnio, dixonMes).isBefore(dixonActual);
    }
}
