package pe.andra.util;

import javafx.application.Platform;

public final class DixonEnFx {

    private DixonEnFx() {
    }

    public static void dixonEjecutar(Runnable dixonTarea) {
        try {
            if (Platform.isFxApplicationThread()) {
                dixonTarea.run();
            } else {
                Platform.runLater(dixonTarea);
            }
        } catch (IllegalStateException dixonSinHerramientas) {
            dixonTarea.run();
        }
    }
}
