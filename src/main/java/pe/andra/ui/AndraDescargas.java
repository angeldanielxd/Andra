package pe.andra.ui;

import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class AndraDescargas {

    private final AndraNotificador andraNotificador;

    public void andraGuardarPdf(String andraNombreSugerido, Supplier<byte[]> andraContenido) {
        FileChooser andraSelectorArchivo = new FileChooser();
        andraSelectorArchivo.setTitle("Guardar como");
        andraSelectorArchivo.setInitialFileName(andraNombreSugerido.replaceAll("[\\\\/:*?\"<>|]", "-") + ".pdf");
        andraSelectorArchivo.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File andraDestino = andraSelectorArchivo.showSaveDialog(andraNotificador.andraVentana());
        if (andraDestino == null) {
            return;
        }
        try {
            Files.write(andraDestino.toPath(), andraContenido.get());
            andraNotificador.andraExito("Archivo guardado: " + andraDestino.getName());
        } catch (IOException | RuntimeException andraError) {
            andraNotificador.andraError("No se pudo guardar el archivo. Intenta en otra carpeta.");
        }
    }
}
