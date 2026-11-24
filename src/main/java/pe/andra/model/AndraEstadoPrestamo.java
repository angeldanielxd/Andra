package pe.andra.model;

import java.util.Arrays;

public enum AndraEstadoPrestamo {
    EN_REVISION("En revisión"),
    APROBADO("Aprobado"),
    DEVUELTO("Devuelto"),
    RECHAZADO("Rechazado"),
    CANCELADO("Cancelado");

    private final String andraEtiqueta;

    AndraEstadoPrestamo(String andraEtiqueta) {
        this.andraEtiqueta = andraEtiqueta;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }

    public boolean andraEsEnCurso() {
        return this == EN_REVISION || this == APROBADO;
    }

    public static AndraEstadoPrestamo andraDesdeEtiqueta(String andraTexto) {
        return Arrays.stream(values()).filter(andraEstadoPrestamo -> andraEstadoPrestamo.andraEtiqueta.equals(andraTexto)).findFirst().orElse(EN_REVISION);
    }
}
