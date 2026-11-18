package pe.andra.model;

public enum DixonCanalAviso {
    WHATSAPP("WhatsApp"),
    CORREO("Correo");

    private final String dixonEtiqueta;

    DixonCanalAviso(String dixonEtiqueta) {
        this.dixonEtiqueta = dixonEtiqueta;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }
}
