package pe.andra.ui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pe.andra.controller.AndraPrincipalControlador;
import pe.andra.model.AndraRol;
import pe.andra.service.AndraCatalogoServicio;
import pe.andra.service.AndraExpedienteServicio;
import pe.andra.service.AndraPagoServicio;
import pe.andra.service.AndraSesionServicio;

import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@EnabledIfSystemProperty(named = "capturas", matches = "true")
class CapturasTest {

    @DynamicPropertySource
    static void base(DynamicPropertyRegistry registro) throws Exception {
        File db = Files.createTempFile("andra-capturas", ".db").toFile();
        db.deleteOnExit();
        registro.add("spring.datasource.url", () -> "jdbc:sqlite:" + db.getAbsolutePath());
    }

    @Autowired
    private ApplicationContext contexto;

    private Stage ventana;
    private AndraPrincipalControlador principal;
    private final File carpeta = new File("target/capturas");

    @Test
    void capturar() throws Exception {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ya) {
            Platform.runLater(() -> {
            });
        }
        carpeta.mkdirs();
        enFx(() -> {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
            cargador.setControllerFactory(contexto::getBean);
            Parent raiz = cargador.load();
            Scene escena = new Scene(raiz, Integer.getInteger("ancho", 1280), Integer.getInteger("alto", 800));
            escena.setCamera(new PerspectiveCamera());
            escena.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/theme.css")).toExternalForm());
            ventana = new Stage();
            ventana.setScene(escena);
            ventana.show();
            principal = cargador.getController();
            principal.andraIniciar(ventana);
            return null;
        });
        esperar(900);
        guardar("00-ingreso");
        String rutas = System.getProperty("rutas", "");
        AndraSesionServicio sesion = contexto.getBean(AndraSesionServicio.class);
        enFx(() -> {
            sesion.andraIngresar(AndraRol.ESTUDIANTE, "20240157", "andra2026");
            principal.andraAlIngresar();
            return null;
        });
        for (String r : List.of("inicio", "documentos", "reservas", "equipos", "solicitudes", "pay", "ayuda", "favoritos", "carne")) {
            if (!rutas.isBlank() && !rutas.contains(r)) {
                continue;
            }
            enFx(() -> {
                principal.andraIrA(r);
                return null;
            });
            esperar(900);
            guardar("e-" + r);
            guardarPagina("e-" + r + "-completa");
        }
        enFx(() -> {
            principal.andraIrA("reservas");
            AndraNavegacion.andraIr(ventana.getScene().getRoot(), "mapa");
            return null;
        });
        esperar(900);
        guardarPagina("e-reservas-mapa");
        enFx(() -> {
            principal.andraIrA("documentos");
            contexto.getBean(AndraAccionesPago.class).andraIniciar(contexto.getBean(AndraCatalogoServicio.class).andraTramite("t-cest").orElseThrow(), null, () -> {
            });
            return null;
        });
        esperar(700);
        guardar("m-asistente-datos");
        enFx(() -> {
            String m = contexto.getBean(AndraPagoServicio.class).andraMatricula().getAndraPago().getAndraCodigo();
            contexto.getBean(AndraAccionesPago.class).andraPantalla(m, () -> {
            });
            return null;
        });
        esperar(700);
        guardar("m-pantalla-pago");
        enFx(() -> {
            contexto.getBean(AndraNotificador.class).andraCerrarModal();
            contexto.getBean(AndraAccionesExpediente.class).andraSeguimiento(contexto.getBean(AndraExpedienteServicio.class).andraMios().get(0).getAndraCodigo());
            return null;
        });
        esperar(700);
        guardar("m-seguimiento");
        enFx(() -> {
            contexto.getBean(AndraNotificador.class).andraCerrarModal();
            principal.andraIrA("inicio");
            return null;
        });
        for (String idioma : List.of("en", "qu", "ay", "es")) {
            enFx(() -> {
                principal.andraCambiarIdioma(idioma);
                return null;
            });
            esperar(700);
            guardar("i-inicio-" + idioma);
        }
        for (String p : List.of("andraEmergenteIdioma", "andraEmergenteNotificaciones", "andraEmergenteEscuela", "andraEmergenteSede", "andraEmergenteUsuario")) {
            enFx(() -> {
                Method m = AndraPrincipalControlador.class.getDeclaredMethod(p);
                m.setAccessible(true);
                m.invoke(principal);
                return null;
            });
            esperar(700);
            guardar("p-" + p);
            enFx(() -> {
                contexto.getBean(AndraNotificador.class).andraCerrarEmergente();
                return null;
            });
        }
        enFx(() -> {
            sesion.andraCerrar();
            sesion.andraIngresar(AndraRol.ADMINISTRADOR, "rmartinez@institucion.edu", "admin2026");
            principal.andraAlIngresar();
            return null;
        });
        for (String r : List.of("admin", "a-datos", "a-tramites", "a-pagos", "a-reservas", "a-prestamos")) {
            if (!rutas.isBlank() && !rutas.contains(r)) {
                continue;
            }
            enFx(() -> {
                principal.andraIrA(r);
                return null;
            });
            esperar(900);
            guardar("a-" + r);
            guardarPagina("a-" + r + "-completa");
        }
        enFx(() -> {
            sesion.andraCerrar();
            ventana.close();
            return null;
        });
    }

    private void guardar(String nombre) throws Exception {
        WritableImage img = enFx(() -> ventana.getScene().snapshot(null));
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File(carpeta, nombre + ".png"));
    }

    private void guardarPagina(String nombre) throws Exception {
        WritableImage img = enFx(() -> {
            StackPane contenido = (StackPane) ventana.getScene().lookup("#andraContenido");
            Node vista = contenido.getChildren().get(0);
            Node pagina = vista instanceof ScrollPane sp ? sp.getContent() : vista;
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.web("#F5F5F7"));
            return pagina.snapshot(sp, null);
        });
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File(carpeta, nombre + ".png"));
    }

    private static void esperar(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    private static <T> T enFx(Callable<T> tarea) throws Exception {
        CompletableFuture<T> f = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                f.complete(tarea.call());
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });
        return f.get(30, TimeUnit.SECONDS);
    }
}
