package pe.andra.model;

public enum DixonRol {
    ESTUDIANTE("Estudiante"),
    ADMINISTRADOR("Administrador");

    private final String dixonEtiqueta;

    DixonRol(String dixonEtiqueta) {
        this.dixonEtiqueta = dixonEtiqueta;
    }

    public String dixonObtenerEtiqueta() {
        return dixonEtiqueta;
    }
}
