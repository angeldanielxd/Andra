package pe.andra.util;

import java.text.Normalizer;
import java.util.Locale;

public final class AndraTextoPlano {

    private AndraTextoPlano() {
    }

    public static String andraNormalizar(String andraTexto) {
        if (andraTexto == null) {
            return "";
        }
        String andraSinTildes = Normalizer.normalize(andraTexto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return andraSinTildes.toLowerCase(Locale.ROOT);
    }

    public static boolean andraContiene(String andraTexto, String andraBusqueda) {
        return andraBusqueda == null || andraBusqueda.isBlank() || andraNormalizar(andraTexto).contains(andraNormalizar(andraBusqueda.trim()));
    }

    public static String andraRecortar(String andraTexto, int andraMaximo) {
        return andraTexto == null || andraTexto.length() <= andraMaximo ? andraTexto : andraTexto.substring(0, andraMaximo - 2) + "…";
    }
}
