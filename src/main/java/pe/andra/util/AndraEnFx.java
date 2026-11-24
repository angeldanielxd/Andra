package pe.andra.util;

import javafx.application.Platform;

public final class AndraEnFx {

    private AndraEnFx() {
    }

    public static void andraEjecutar(Runnable andraTarea) {
        try {
            if (Platform.isFxApplicationThread()) {
                andraTarea.run();
            } else {
                Platform.runLater(andraTarea);
            }
        } catch (IllegalStateException andraSinHerramientas) {
            andraTarea.run();
        }
    }
}
