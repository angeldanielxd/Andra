package pe.andra.ui;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;

public class AndraNavegacion extends Event {

    public static final EventType<AndraNavegacion> ANDRA_IR = new EventType<>(Event.ANY, "ANDRA_NAVEGAR");

    private final String andraRuta;

    public AndraNavegacion(String andraRuta) {
        super(ANDRA_IR);
        this.andraRuta = andraRuta;
    }

    public String andraObtenerRuta() {
        return andraRuta;
    }

    public static void andraIr(Node andraOrigen, String andraRuta) {
        if (andraOrigen != null) {
            andraOrigen.fireEvent(new AndraNavegacion(andraRuta));
        }
    }
}
