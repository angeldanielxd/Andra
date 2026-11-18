package pe.andra.model;

import java.util.Arrays;

public enum DixonGrupoExpediente {
    TRAMITE("tramite", "En trámite", "warning"),
    LISTO("listo", "Emitidos / Listos", "success"),
    OBSERVADO("observado", "Observados", "error"),
    ANULADO("anulado", "Anulados", "neutral");

    private final String dixonClave;
    private final String dixonEtiqueta;
    private final String dixonTono;

    DixonGrupoExpediente(String dixonClave, String dixonEtiqueta, String dixonTono) {
        this.dixonClave = dixonClave;
        this.dixonEtiqueta = dixonEtiqueta;
        this.dixonTono = dixonTono;
    }

    public String dixonObtenerClave() {
        return dixonClave;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }

    public String dixonObtenerTono() {
        return dixonTono;
    }

    public static DixonGrupoExpediente dixonDesdeClave(String dixonClave) {
        return Arrays.stream(values()).filter(dixonGrupoExpediente -> dixonGrupoExpediente.dixonClave.equals(dixonClave)).findFirst().orElse(TRAMITE);
    }
}
