package pe.andra.model;

public enum DixonMetodoPago {
    YAPE("Yape", "YP", false),
    PLIN("Plin", "PL", false),
    VISA("Visa", "TC", true),
    MASTERCARD("Mastercard", "TC", true),
    AMEX("American Express", "TC", true),
    DINERS("Diners Club", "TC", true);

    private final String dixonEtiqueta;
    private final String dixonPrefijo;
    private final boolean dixonTarjeta;

    DixonMetodoPago(String dixonEtiqueta, String dixonPrefijo, boolean dixonTarjeta) {
        this.dixonEtiqueta = dixonEtiqueta;
        this.dixonPrefijo = dixonPrefijo;
        this.dixonTarjeta = dixonTarjeta;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }

    public String dixonObtenerPrefijo() {
        return dixonPrefijo;
    }

    public boolean dixonEsTarjeta() {
        return dixonTarjeta;
    }

    public String dixonObtenerGrupo() {
        return dixonTarjeta ? "Tarjeta" : dixonEtiqueta;
    }

    public static DixonMetodoPago dixonDesdeClave(String dixonClave) {
        return dixonClave == null ? null : valueOf(dixonClave.toUpperCase());
    }
}
