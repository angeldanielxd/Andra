package pe.andra.model;

public enum AndraTipoPago {
    MATRICULA("Matrícula", "fth-award"),
    DOCUMENTO("Documento", "fth-file-text"),
    CARNE("Carné", "fth-credit-card"),
    TRAMITE("Trámite", "fth-file");

    private final String andraEtiqueta;
    private final String andraIcono;

    AndraTipoPago(String andraEtiqueta, String andraIcono) {
        this.andraEtiqueta = andraEtiqueta;
        this.andraIcono = andraIcono;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }

    public String andraObtenerIcono() {
        return andraIcono;
    }

    public static AndraTipoPago andraDesdeClave(String andraClave) {
        return valueOf(andraClave.toUpperCase());
    }
}
