package pe.andra.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DixonEstadoFiltro {

    private final Set<String> dixonActivos = new LinkedHashSet<>();
    private final Map<String, String> dixonAvanzados = new LinkedHashMap<>();
    private String dixonTexto = "";

    public Set<String> dixonObtenerActivos() {
        return dixonActivos;
    }

    public Map<String, String> dixonObtenerAvanzados() {
        return dixonAvanzados;
    }

    public String dixonObtenerTexto() {
        return dixonTexto;
    }

    public void dixonAsignarTexto(String dixonTexto) {
        this.dixonTexto = dixonTexto == null ? "" : dixonTexto.trim();
    }

    public void dixonAlternar(String dixonClave) {
        if (!dixonActivos.remove(dixonClave)) {
            dixonActivos.add(dixonClave);
        }
    }

    public String dixonAvanzado(String dixonClave) {
        String dixonValor = dixonAvanzados.get(dixonClave);
        return dixonValor == null || dixonValor.isBlank() ? null : dixonValor;
    }

    public int dixonCantidad() {
        return dixonActivos.size() + (dixonTexto.isBlank() ? 0 : 1)
                + (int) dixonAvanzados.values().stream().filter(dixonValor -> dixonValor != null && !dixonValor.isBlank()).count();
    }

    public void dixonLimpiar() {
        dixonActivos.clear();
        dixonAvanzados.clear();
        dixonTexto = "";
    }
}
