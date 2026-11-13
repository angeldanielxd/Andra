package pe.andra.ui;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;

public class Navegacion extends Event {

    public static final EventType<Navegacion> IR = new EventType<>(Event.ANY, "ANDRA_NAVEGAR");

    private final String ruta;

    public Navegacion(String ruta) {
        super(IR);
        this.ruta = ruta;
    }

    public String getRuta() {
        return ruta;
    }

    public static void ir(Node origen, String ruta) {
        if (origen != null) {
            origen.fireEvent(new Navegacion(ruta));
        }
    }
}
