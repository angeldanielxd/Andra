package pe.andra.model;

public enum AndraMetodoPago {
    YAPE("Yape", "YP", false),
    PLIN("Plin", "PL", false),
    VISA("Visa", "TC", true),
    MASTERCARD("Mastercard", "TC", true),
    AMEX("American Express", "TC", true),
    DINERS("Diners Club", "TC", true);

    private final String andraEtiqueta;
    private final String andraPrefijo;
    private final boolean andraTarjeta;

    AndraMetodoPago(String andraEtiqueta, String andraPrefijo, boolean andraTarjeta) {
        this.andraEtiqueta = andraEtiqueta;
        this.andraPrefijo = andraPrefijo;
        this.andraTarjeta = andraTarjeta;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }

    public String andraObtenerPrefijo() {
        return andraPrefijo;
    }

    public boolean andraEsTarjeta() {
        return andraTarjeta;
    }

    public String andraObtenerGrupo() {
        return andraTarjeta ? "Tarjeta" : andraEtiqueta;
    }

    public static AndraMetodoPago andraDesdeClave(String andraClave) {
        return andraClave == null ? null : valueOf(andraClave.toUpperCase());
    }
}
