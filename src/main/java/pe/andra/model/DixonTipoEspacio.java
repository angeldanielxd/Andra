package pe.andra.model;

import java.util.Arrays;

public enum DixonTipoEspacio {
    AULA("Aula", "Aulas"),
    AUDITORIO("Auditorio", "Auditorios"),
    LABORATORIO("Laboratorio", "Laboratorios"),
    SALA("Sala", "Salas"),
    TALLER("Taller", "Talleres");

    private final String dixonEtiqueta;
    private final String dixonPlural;

    DixonTipoEspacio(String dixonEtiqueta, String dixonPlural) {
        this.dixonEtiqueta = dixonEtiqueta;
        this.dixonPlural = dixonPlural;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }

    public String dixonObtenerPlural() {
        return dixonPlural;
    }

    public static DixonTipoEspacio dixonDesdeEtiqueta(String dixonTexto) {
        return Arrays.stream(values()).filter(dixonTipoEspacio -> dixonTipoEspacio.dixonEtiqueta.equalsIgnoreCase(dixonTexto)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(dixonTexto));
    }
}
