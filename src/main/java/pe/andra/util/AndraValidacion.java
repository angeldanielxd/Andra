package pe.andra.util;

public final class AndraValidacion {

    private AndraValidacion() {
    }

    public static boolean andraDni(String andraValor) {
        return andraValor != null && andraValor.trim().matches("\\d{8}");
    }

    public static boolean andraCelular(String andraValor) {
        return andraValor != null && andraValor.replaceAll("\\s", "").matches("9\\d{8}");
    }

    public static boolean andraCorreo(String andraValor) {
        return andraValor != null && andraValor.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    public static boolean andraCodigoAprobacion(String andraValor) {
        return andraValor != null && andraValor.trim().matches("\\d{6}");
    }

    public static boolean andraCvv(String andraValor) {
        return andraValor != null && andraValor.trim().matches("\\d{3,4}");
    }

    public static String andraSoloDigitos(String andraValor) {
        return andraValor == null ? "" : andraValor.replaceAll("\\D", "");
    }
}
