package pe.andra.ui;

import java.util.ArrayList;
import java.util.List;

public record AndraOpcion(String andraValor, String andraEtiqueta, boolean andraGrupo) {

    public static AndraOpcion andraDe(String andraValor, String andraEtiqueta) {
        return new AndraOpcion(andraValor, andraEtiqueta, false);
    }

    public static AndraOpcion andraGrupo(String andraEtiqueta) {
        return new AndraOpcion("", andraEtiqueta, true);
    }

    public static List<AndraOpcion> andraLista(List<String> andraValores) {
        List<AndraOpcion> andraLista = new ArrayList<>();
        andraValores.forEach(andraValor2 -> andraLista.add(andraDe(andraValor2, andraValor2)));
        return andraLista;
    }

    public static List<AndraOpcion> andraPares(List<String[]> andraPares) {
        List<AndraOpcion> andraLista = new ArrayList<>();
        andraPares.forEach(andraTextos -> andraLista.add(andraDe(andraTextos[0], andraTextos[1])));
        return andraLista;
    }
}
