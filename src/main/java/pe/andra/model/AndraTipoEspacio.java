package pe.andra.model;

import java.util.Arrays;

public enum AndraTipoEspacio {
    AULA("Aula", "Aulas"),
    AUDITORIO("Auditorio", "Auditorios"),
    LABORATORIO("Laboratorio", "Laboratorios"),
    SALA("Sala", "Salas"),
    TALLER("Taller", "Talleres");

    private final String andraEtiqueta;
    private final String andraPlural;

    AndraTipoEspacio(String andraEtiqueta, String andraPlural) {
        this.andraEtiqueta = andraEtiqueta;
        this.andraPlural = andraPlural;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }

    public String andraObtenerPlural() {
        return andraPlural;
    }

    public static AndraTipoEspacio andraDesdeEtiqueta(String andraTexto) {
        return Arrays.stream(values()).filter(andraTipoEspacio -> andraTipoEspacio.andraEtiqueta.equalsIgnoreCase(andraTexto)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(andraTexto));
    }
}
