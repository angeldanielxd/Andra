package pe.andra.model;

import java.util.Arrays;

public enum DixonEstadoReserva {
    APROBADO("Aprobado"),
    PENDIENTE("Pendiente"),
    RECHAZADO("Rechazado"),
    VENCIO("Venció"),
    CANCELADO("Cancelado");

    private final String dixonEtiqueta;

    DixonEstadoReserva(String dixonEtiqueta) {
        this.dixonEtiqueta = dixonEtiqueta;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }

    public boolean dixonEsActiva() {
        return this == APROBADO || this == PENDIENTE;
    }

    public static DixonEstadoReserva dixonDesdeEtiqueta(String dixonTexto) {
        return Arrays.stream(values()).filter(dixonEstadoReserva -> dixonEstadoReserva.dixonEtiqueta.equals(dixonTexto)).findFirst().orElse(PENDIENTE);
    }
}
