package pe.andra.model;

import java.util.Arrays;

public enum AndraEstadoReserva {
    APROBADO("Aprobado"),
    PENDIENTE("Pendiente"),
    RECHAZADO("Rechazado"),
    VENCIO("Venció"),
    CANCELADO("Cancelado");

    private final String andraEtiqueta;

    AndraEstadoReserva(String andraEtiqueta) {
        this.andraEtiqueta = andraEtiqueta;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }

    public boolean andraEsActiva() {
        return this == APROBADO || this == PENDIENTE;
    }

    public static AndraEstadoReserva andraDesdeEtiqueta(String andraTexto) {
        return Arrays.stream(values()).filter(andraEstadoReserva -> andraEstadoReserva.andraEtiqueta.equals(andraTexto)).findFirst().orElse(PENDIENTE);
    }
}
