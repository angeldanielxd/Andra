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
import pe.andra.model.DixonCambioPendiente;
import pe.andra.repository.DixonCambioPendienteRepositorio;
import pe.andra.util.DixonEnFx;
import pe.andra.util.DixonFechas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DixonConexionServicio {

    private final DixonCambioPendienteRepositorio dixonCola;
    private final String dixonUrlPrueba;
    private final BooleanProperty dixonRedDisponible = new SimpleBooleanProperty(true);
    private final BooleanProperty dixonSimulado = new SimpleBooleanProperty(false);
    private final IntegerProperty dixonPendientes = new SimpleIntegerProperty(0);
    private final BooleanBinding dixonEnLinea;
    private ScheduledExecutorService dixonVigia;

    public DixonConexionServicio(DixonCambioPendienteRepositorio dixonCola, @Value("${app.conexion.url-prueba:https://www.google.com}") String dixonUrlPrueba) {
        this.dixonCola = dixonCola;
        this.dixonUrlPrueba = dixonUrlPrueba;
        this.dixonEnLinea = Bindings.createBooleanBinding(() -> dixonRedDisponible.get() && !dixonSimulado.get(), dixonRedDisponible, dixonSimulado);
    }

    public void dixonIniciarMonitoreo() {
        if (dixonVigia != null) {
            return;
        }
        dixonPendientes.set((int) dixonCola.count());
        HttpClient dixonCliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
        dixonVigia = Executors.newSingleThreadScheduledExecutor(dixonAccion -> {
            Thread dixonHilo = new Thread(dixonAccion, "andra-conexion");
            dixonHilo.setDaemon(true);
            return dixonHilo;
        });
        dixonVigia.scheduleWithFixedDelay(() -> {
            boolean dixonCorrecto;
            try {
                HttpRequest dixonPedido = HttpRequest.newBuilder(URI.create(dixonUrlPrueba)).method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(4)).build();
                dixonCorrecto = dixonCliente.send(dixonPedido, HttpResponse.BodyHandlers.discarding()).statusCode() < 500;
            } catch (Exception dixonError) {
                dixonCorrecto = false;
            }
            boolean dixonResultado = dixonCorrecto;
            DixonEnFx.dixonEjecutar(() -> dixonRedDisponible.set(dixonResultado));
        }, 2, 15, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void dixonDetener() {
        if (dixonVigia != null) {
            dixonVigia.shutdownNow();
        }
    }

    public BooleanBinding dixonEnLineaPropiedad() {
        return dixonEnLinea;
    }

    public boolean dixonEnLinea() {
        return dixonEnLinea.get();
    }

    public boolean dixonSinConexion() {
        return !dixonEnLinea.get();
    }

    public BooleanProperty dixonSimuladoPropiedad() {
        return dixonSimulado;
    }

    public BooleanProperty dixonRedPropiedad() {
        return dixonRedDisponible;
    }

    public boolean dixonEsSimulado() {
        return dixonSimulado.get();
    }

    public IntegerProperty dixonPendientesPropiedad() {
        return dixonPendientes;
    }

    public void dixonSimularSinConexion(boolean dixonValor) {
        dixonSimulado.set(dixonValor);
    }

    @Transactional
    public void dixonEncolar(String dixonTexto) {
        if (dixonEnLinea()) {
            return;
        }
        dixonCola.save(DixonCambioPendiente.builder().dixonTexto(dixonTexto).dixonCreado(DixonFechas.dixonAhora()).build());
        dixonPendientes.set((int) dixonCola.count());
    }

    @Transactional(readOnly = true)
    public int dixonContarPendientes() {
        return (int) dixonCola.count();
    }

    @Transactional
    public int dixonSincronizar() {
        int dixonCantidad = (int) dixonCola.count();
        dixonCola.deleteAllInBatch();
        dixonPendientes.set(0);
        return dixonCantidad;
    }
}
