package pe.andra.util;

import java.text.Normalizer;
import java.util.Locale;

public final class DixonTextoPlano {

    private DixonTextoPlano() {
    }

    public static String dixonNormalizar(String dixonTexto) {
        if (dixonTexto == null) {
            return "";
        }
        String dixonSinTildes = Normalizer.normalize(dixonTexto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return dixonSinTildes.toLowerCase(Locale.ROOT);
    }

    public static boolean dixonContiene(String dixonTexto, String dixonBusqueda) {
        return dixonBusqueda == null || dixonBusqueda.isBlank() || dixonNormalizar(dixonTexto).contains(dixonNormalizar(dixonBusqueda.trim()));
    }

    public static String dixonRecortar(String dixonTexto, int dixonMaximo) {
        return dixonTexto == null || dixonTexto.length() <= dixonMaximo ? dixonTexto : dixonTexto.substring(0, dixonMaximo - 2) + "…";
    }
}
