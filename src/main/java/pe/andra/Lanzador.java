package pe.andra;

import javafx.application.Application;

import java.io.File;

public class Lanzador {
    public static void main(String[] args) {
        configurarBaseInstalada();
        Application.launch(AplicacionFx.class, args);
    }

    /**
     * Cuando la app corre instalada (empaquetada con jpackage), la carpeta de trabajo no es escribible,
     * así que la base SQLite se guarda en la carpeta de datos del usuario. En desarrollo no cambia nada.
     */
    private static void configurarBaseInstalada() {
        if (System.getProperty("jpackage.app-path") == null || System.getProperty("spring.datasource.url") != null) {
            return;
        }
        String home = System.getProperty("user.home");
        String so = System.getProperty("os.name", "").toLowerCase();
        File carpeta;
        if (so.contains("mac")) {
            carpeta = new File(home, "Library/Application Support/Andra");
        } else if (so.contains("win") && System.getenv("APPDATA") != null) {
            carpeta = new File(System.getenv("APPDATA"), "Andra");
        } else {
            carpeta = new File(home, ".andra");
        }
        carpeta.mkdirs();
        System.setProperty("spring.datasource.url", "jdbc:sqlite:" + new File(carpeta, "andra.db").getAbsolutePath());
    }
}
