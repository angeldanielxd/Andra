package pe.andra;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LanzadorTest {

    @TempDir
    Path carpeta;

    @Test
    void respaldaLaBaseDeUnaVersionAnteriorYDejaLaMarca() throws Exception {
        File base = carpeta.resolve("andra.db").toFile();
        Files.writeString(base.toPath(), "datos 1.0.0");

        Lanzador.renovarSiCambioLaVersion(carpeta.toFile(), base);

        assertThat(base).doesNotExist();
        assertThat(carpeta.resolve("version-datos.txt")).hasContent(Lanzador.VERSION_DATOS);
        try (var archivos = Files.list(carpeta)) {
            assertThat(archivos.map(p -> p.getFileName().toString()))
                    .anyMatch(n -> n.startsWith("andra-respaldo-1.0.0-") && n.endsWith(".db"));
        }
    }

    @Test
    void noTocaLaBaseSiYaEsDeLaVersionActual() throws Exception {
        File base = carpeta.resolve("andra.db").toFile();
        Files.writeString(base.toPath(), "datos actuales");
        Files.writeString(carpeta.resolve("version-datos.txt"), Lanzador.VERSION_DATOS);

        Lanzador.renovarSiCambioLaVersion(carpeta.toFile(), base);

        assertThat(base).hasContent("datos actuales");
    }

    @Test
    void enUnaInstalacionNuevaSoloEscribeLaMarca() {
        File base = carpeta.resolve("andra.db").toFile();

        Lanzador.renovarSiCambioLaVersion(carpeta.toFile(), base);

        assertThat(base).doesNotExist();
        assertThat(carpeta.resolve("version-datos.txt")).hasContent(Lanzador.VERSION_DATOS);
    }
}
