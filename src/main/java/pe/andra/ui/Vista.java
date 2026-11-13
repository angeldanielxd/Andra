package pe.andra.ui;

public interface Vista {
    void refrescar();

    default void salir() {
    }

    default void enfocarBusqueda() {
    }
}
