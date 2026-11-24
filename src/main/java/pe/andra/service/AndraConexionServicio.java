package pe.andra.service;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.AndraCambioPendiente;
import pe.andra.repository.AndraCambioPendienteRepositorio;
import pe.andra.util.AndraEnFx;
import pe.andra.util.AndraFechas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AndraConexionServicio {

    private final AndraCambioPendienteRepositorio andraCola;
    private final String andraUrlPrueba;
    private final BooleanProperty andraRedDisponible = new SimpleBooleanProperty(true);
    private final BooleanProperty andraSimulado = new SimpleBooleanProperty(false);
    private final IntegerProperty andraPendientes = new SimpleIntegerProperty(0);
    private final BooleanBinding andraEnLinea;
    private ScheduledExecutorService andraVigia;

    public AndraConexionServicio(AndraCambioPendienteRepositorio andraCola, @Value("${app.conexion.url-prueba:https://www.google.com}") String andraUrlPrueba) {
        this.andraCola = andraCola;
        this.andraUrlPrueba = andraUrlPrueba;
        this.andraEnLinea = Bindings.createBooleanBinding(() -> andraRedDisponible.get() && !andraSimulado.get(), andraRedDisponible, andraSimulado);
    }

    public void andraIniciarMonitoreo() {
        if (andraVigia != null) {
            return;
        }
        andraPendientes.set((int) andraCola.count());
        HttpClient andraCliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
        andraVigia = Executors.newSingleThreadScheduledExecutor(andraAccion -> {
            Thread andraHilo = new Thread(andraAccion, "andra-conexion");
            andraHilo.setDaemon(true);
            return andraHilo;
        });
        andraVigia.scheduleWithFixedDelay(() -> {
            boolean andraCorrecto;
            try {
                HttpRequest andraPedido = HttpRequest.newBuilder(URI.create(andraUrlPrueba)).method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(4)).build();
                andraCorrecto = andraCliente.send(andraPedido, HttpResponse.BodyHandlers.discarding()).statusCode() < 500;
            } catch (Exception andraError) {
                andraCorrecto = false;
            }
            boolean andraResultado = andraCorrecto;
            AndraEnFx.andraEjecutar(() -> andraRedDisponible.set(andraResultado));
        }, 2, 15, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void andraDetener() {
        if (andraVigia != null) {
            andraVigia.shutdownNow();
        }
    }

    public BooleanBinding andraEnLineaPropiedad() {
        return andraEnLinea;
    }

    public boolean andraEnLinea() {
        return andraEnLinea.get();
    }

    public boolean andraSinConexion() {
        return !andraEnLinea.get();
    }

    public BooleanProperty andraSimuladoPropiedad() {
        return andraSimulado;
    }

    public BooleanProperty andraRedPropiedad() {
        return andraRedDisponible;
    }

    public boolean andraEsSimulado() {
        return andraSimulado.get();
    }

    public IntegerProperty andraPendientesPropiedad() {
        return andraPendientes;
    }

    public void andraSimularSinConexion(boolean andraValor) {
        andraSimulado.set(andraValor);
    }

    @Transactional
    public void andraEncolar(String andraTexto) {
        if (andraEnLinea()) {
            return;
        }
        andraCola.save(AndraCambioPendiente.builder().andraTexto(andraTexto).andraCreado(AndraFechas.andraAhora()).build());
        andraPendientes.set((int) andraCola.count());
    }

    @Transactional(readOnly = true)
    public int andraContarPendientes() {
        return (int) andraCola.count();
    }

    @Transactional
    public int andraSincronizar() {
        int andraCantidad = (int) andraCola.count();
        andraCola.deleteAllInBatch();
        andraPendientes.set(0);
        return andraCantidad;
    }
}
