package pe.andra.util;

public final class DixonValidacion {

    private DixonValidacion() {
    }

    public static boolean dixonDni(String dixonValor) {
        return dixonValor != null && dixonValor.trim().matches("\\d{8}");
    }

    public static boolean dixonCelular(String dixonValor) {
        return dixonValor != null && dixonValor.replaceAll("\\s", "").matches("9\\d{8}");
    }

    public static boolean dixonCorreo(String dixonValor) {
        return dixonValor != null && dixonValor.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    public static boolean dixonCodigoAprobacion(String dixonValor) {
        return dixonValor != null && dixonValor.trim().matches("\\d{6}");
    }

    public static boolean dixonCvv(String dixonValor) {
        return dixonValor != null && dixonValor.trim().matches("\\d{3,4}");
    }

    public static String dixonSoloDigitos(String dixonValor) {
        return dixonValor == null ? "" : dixonValor.replaceAll("\\D", "");
    }
}
