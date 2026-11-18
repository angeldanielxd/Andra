package pe.andra.model;

public enum DixonTipoPago {
    MATRICULA("Matrícula", "fth-award"),
    DOCUMENTO("Documento", "fth-file-text"),
    CARNE("Carné", "fth-credit-card"),
    TRAMITE("Trámite", "fth-file");

    private final String dixonEtiqueta;
    private final String dixonIcono;

    DixonTipoPago(String dixonEtiqueta, String dixonIcono) {
        this.dixonEtiqueta = dixonEtiqueta;
        this.dixonIcono = dixonIcono;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }

    public String dixonObtenerIcono() {
        return dixonIcono;
    }

    public static DixonTipoPago dixonDesdeClave(String dixonClave) {
        return valueOf(dixonClave.toUpperCase());
    }
}
