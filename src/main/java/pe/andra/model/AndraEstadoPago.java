package pe.andra.model;

public enum AndraEstadoPago {
    PENDIENTE("Por pagar"),
    PAGADO("Pagado"),
    ANULADO("Anulado"),
    REEMBOLSADO("Reembolsado");

    private final String andraEtiqueta;

    AndraEstadoPago(String andraEtiqueta) {
        this.andraEtiqueta = andraEtiqueta;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }
}
