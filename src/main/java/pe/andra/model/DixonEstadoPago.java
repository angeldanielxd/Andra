package pe.andra.model;

public enum DixonEstadoPago {
    PENDIENTE("Por pagar"),
    PAGADO("Pagado"),
    ANULADO("Anulado"),
    REEMBOLSADO("Reembolsado");

    private final String dixonEtiqueta;

    DixonEstadoPago(String dixonEtiqueta) {
        this.dixonEtiqueta = dixonEtiqueta;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }
}
