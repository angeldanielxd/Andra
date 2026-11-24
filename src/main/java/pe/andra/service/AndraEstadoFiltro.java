package pe.andra.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AndraEstadoFiltro {

    private final Set<String> andraActivos = new LinkedHashSet<>();
    private final Map<String, String> andraAvanzados = new LinkedHashMap<>();
    private String andraTexto = "";

    public Set<String> andraObtenerActivos() {
        return andraActivos;
    }

    public Map<String, String> andraObtenerAvanzados() {
        return andraAvanzados;
    }

    public String andraObtenerTexto() {
        return andraTexto;
    }

    public void andraAsignarTexto(String andraTexto) {
        this.andraTexto = andraTexto == null ? "" : andraTexto.trim();
    }

    public void andraAlternar(String andraClave) {
        if (!andraActivos.remove(andraClave)) {
            andraActivos.add(andraClave);
        }
    }

    public String andraAvanzado(String andraClave) {
        String andraValor = andraAvanzados.get(andraClave);
        return andraValor == null || andraValor.isBlank() ? null : andraValor;
    }

    public int andraCantidad() {
        return andraActivos.size() + (andraTexto.isBlank() ? 0 : 1)
                + (int) andraAvanzados.values().stream().filter(andraValor -> andraValor != null && !andraValor.isBlank()).count();
    }

    public void andraLimpiar() {
        andraActivos.clear();
        andraAvanzados.clear();
        andraTexto = "";
    }
}
