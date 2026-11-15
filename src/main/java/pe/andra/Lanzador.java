package pe.andra;

import javafx.application.Application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Lanzador {

    /**
     * Versión de los datos de demostración (catalogo.json y datos-demo.json). Súbela cuando cambien:
     * la app instalada guarda la base anterior como respaldo y vuelve a sembrar los datos nuevos.
     */
    static final String VERSION_DATOS = "1.1.0";

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
        File base = new File(carpeta, "andra.db");
        renovarSiCambioLaVersion(carpeta, base);
        System.setProperty("spring.datasource.url", "jdbc:sqlite:" + base.getAbsolutePath());
    }

    /**
     * Si la base fue creada con otros datos de demostración, la mueve a un respaldo
     * (andra-respaldo-&lt;versión&gt;-&lt;fecha&gt;.db) para que el seeder cree una base limpia.
     */
    static void renovarSiCambioLaVersion(File carpeta, File base) {
        File marca = new File(carpeta, "version-datos.txt");
        String anterior = null;
        if (marca.isFile()) {
            try {
                anterior = Files.readString(marca.toPath(), StandardCharsets.UTF_8).trim();
            } catch (IOException e) {
                anterior = null;
            }
        }
        if (VERSION_DATOS.equals(anterior)) {
            return;
        }
        if (base.isFile()) {
            String version = anterior == null || anterior.isBlank() ? "1.0.0" : anterior;
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String respaldo = "andra-respaldo-" + version + "-" + fecha + ".db";
            for (String extra : new String[]{"", "-journal", "-wal", "-shm"}) {
                File origen = new File(base.getPath() + extra);
                if (origen.isFile() && !origen.renameTo(new File(carpeta, respaldo + extra)) && extra.isEmpty()) {
                    return;
                }
            }
        }
        try {
            Files.writeString(marca.toPath(), VERSION_DATOS, StandardCharsets.UTF_8);
        } catch (IOException ignorada) {
            // Sin marca, se volverá a intentar en el próximo inicio.
        }
    }
}
