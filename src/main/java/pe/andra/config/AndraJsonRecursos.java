package pe.andra.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

@Component
public class AndraJsonRecursos {

    private final ObjectMapper andraLector = new ObjectMapper();
    private JsonNode andraCatalogo;
    private JsonNode andraDemo;
    private JsonNode andraTraducciones;

    public synchronized JsonNode andraCatalogo() {
        if (andraCatalogo == null) {
            andraCatalogo = andraLeer("/datos/catalogo.json");
        }
        return andraCatalogo;
    }

    public synchronized JsonNode andraDemo() {
        if (andraDemo == null) {
            andraDemo = andraLeer("/datos/datos-demo.json");
        }
        return andraDemo;
    }

    public synchronized JsonNode andraTraducciones() {
        if (andraTraducciones == null) {
            andraTraducciones = andraLeer("/i18n/traducciones.json");
        }
        return andraTraducciones;
    }

    private JsonNode andraLeer(String andraRuta) {
        try (InputStream andraEntrada = Objects.requireNonNull(getClass().getResourceAsStream(andraRuta), andraRuta)) {
            return andraLector.readTree(andraEntrada);
        } catch (IOException andraError) {
            throw new UncheckedIOException("No se pudo leer " + andraRuta, andraError);
        }
    }
}
