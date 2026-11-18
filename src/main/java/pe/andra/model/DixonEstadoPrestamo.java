package pe.andra.model;

import java.util.Arrays;

public enum DixonEstadoPrestamo {
    EN_REVISION("En revisión"),
    APROBADO("Aprobado"),
    DEVUELTO("Devuelto"),
    RECHAZADO("Rechazado"),
    CANCELADO("Cancelado");

    private final String dixonEtiqueta;

    DixonEstadoPrestamo(String dixonEtiqueta) {
        this.dixonEtiqueta = dixonEtiqueta;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }

    public boolean dixonEsEnCurso() {
        return this == EN_REVISION || this == APROBADO;
    }

    public static DixonEstadoPrestamo dixonDesdeEtiqueta(String dixonTexto) {
        return Arrays.stream(values()).filter(dixonEstadoPrestamo -> dixonEstadoPrestamo.dixonEtiqueta.equals(dixonTexto)).findFirst().orElse(EN_REVISION);
    }
}
