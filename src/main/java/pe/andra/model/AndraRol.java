package pe.andra.model;

public enum AndraRol {
    ESTUDIANTE("Estudiante"),
    ADMINISTRADOR("Administrador");

    private final String andraEtiqueta;

    AndraRol(String andraEtiqueta) {
        this.andraEtiqueta = andraEtiqueta;
    }

    public String andraObtenerEtiqueta() {
        return andraEtiqueta;
    }
}
