package pe.andra;

import javafx.application.Application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AndraLanzador {

    static final String ANDRA_VERSION_DATOS = "1.1.0";

    public static void main(String[] andraArgs) {
        andraConfigurarBaseInstalada();
        Application.launch(AndraAplicacionFx.class, andraArgs);
    }

    private static void andraConfigurarBaseInstalada() {
        if (System.getProperty("jpackage.app-path") == null || System.getProperty("spring.datasource.url") != null) {
            return;
        }
        String andraInicio = System.getProperty("user.home");
        String andraSistemaOperativo = System.getProperty("os.name", "").toLowerCase();
        File andraCarpeta;
        if (andraSistemaOperativo.contains("mac")) {
            andraCarpeta = new File(andraInicio, "Library/Application Support/Andra");
        } else if (andraSistemaOperativo.contains("win") && System.getenv("APPDATA") != null) {
            andraCarpeta = new File(System.getenv("APPDATA"), "Andra");
        } else {
            andraCarpeta = new File(andraInicio, ".andra");
        }
        andraCarpeta.mkdirs();
        File andraBase = new File(andraCarpeta, "andra.db");
        andraRenovarSiCambioLaVersion(andraCarpeta, andraBase);
        System.setProperty("spring.datasource.url", "jdbc:sqlite:" + andraBase.getAbsolutePath());
    }

    static void andraRenovarSiCambioLaVersion(File andraCarpeta, File andraBase) {
        File andraMarca = new File(andraCarpeta, "version-datos.txt");
        String andraAnterior = null;
        if (andraMarca.isFile()) {
            try {
                andraAnterior = Files.readString(andraMarca.toPath(), StandardCharsets.UTF_8).trim();
            } catch (IOException andraError) {
                andraAnterior = null;
            }
        }
        if (ANDRA_VERSION_DATOS.equals(andraAnterior)) {
            return;
        }
        if (andraBase.isFile()) {
            String andraVersion = andraAnterior == null || andraAnterior.isBlank() ? "1.0.0" : andraAnterior;
            String andraFecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String andraRespaldo = "andra-respaldo-" + andraVersion + "-" + andraFecha + ".db";
            for (String andraExtra : new String[]{"", "-journal", "-wal", "-shm"}) {
                File andraOrigen = new File(andraBase.getPath() + andraExtra);
                if (andraOrigen.isFile() && !andraOrigen.renameTo(new File(andraCarpeta, andraRespaldo + andraExtra)) && andraExtra.isEmpty()) {
                    return;
                }
            }
        }
        try {
            Files.writeString(andraMarca.toPath(), ANDRA_VERSION_DATOS, StandardCharsets.UTF_8);
        } catch (IOException andraIgnorada) {
        }
    }
}
