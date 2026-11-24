package pe.andra.model;

public enum AndraCanalAviso {
    WHATSAPP("WhatsApp"),
    CORREO("Correo");

    private final String andraEtiqueta;

    AndraCanalAviso(String andraEtiqueta) {
        this.andraEtiqueta = andraEtiqueta;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }
}
