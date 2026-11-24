package pe.andra.model;

import java.util.Arrays;

public enum AndraGrupoExpediente {
    TRAMITE("tramite", "En trámite", "warning"),
    LISTO("listo", "Emitidos / Listos", "success"),
    OBSERVADO("observado", "Observados", "error"),
    ANULADO("anulado", "Anulados", "neutral");

    private final String andraClave;
    private final String andraEtiqueta;
    private final String andraTono;

    AndraGrupoExpediente(String andraClave, String andraEtiqueta, String andraTono) {
        this.andraClave = andraClave;
        this.andraEtiqueta = andraEtiqueta;
        this.andraTono = andraTono;
    }

    public String andraObtenerClave() {
        return andraClave;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }

    public String andraObtenerTono() {
        return andraTono;
    }

    public static AndraGrupoExpediente andraDesdeClave(String andraClave) {
        return Arrays.stream(values()).filter(andraGrupoExpediente -> andraGrupoExpediente.andraClave.equals(andraClave)).findFirst().orElse(TRAMITE);
    }
}
