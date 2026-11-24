package pe.andra;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import pe.andra.controller.AndraPrincipalControlador;
import pe.andra.model.AndraAjustes;
import pe.andra.service.AndraAjustesServicio;
import pe.andra.service.AndraConexionServicio;
import pe.andra.ui.AndraEnlaces;

import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

public class AndraAplicacionFx extends Application {

    private ConfigurableApplicationContext andraContexto;
    private AndraPrincipalControlador andraPrincipal;

    @Override
    public void init() {
        Locale.setDefault(Locale.forLanguageTag("es-PE"));
        andraContexto = new SpringApplicationBuilder(AndraAplicacion.class).web(WebApplicationType.NONE).run();
    }

    @Override
    public void start(Stage andraVentana) throws Exception {
        boolean andraMac = System.getProperty("os.name", "").toLowerCase().contains("mac");
        for (String andraPeso : new String[]{"400", "500", "600"}) {
            try (InputStream andraEntrada = getClass().getResourceAsStream("/fuentes/Inter-" + andraPeso + ".ttf")) {
                if (andraEntrada != null) {
                    Font.loadFont(andraEntrada, 14);
                }
            }
        }
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        andraContexto.getBean(AndraEnlaces.class).andraAsignarServiciosAnfitrion(getHostServices());

        FXMLLoader andraCargador = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
        andraCargador.setControllerFactory(andraContexto::getBean);
        Parent andraRaiz = andraCargador.load();
        if (!andraMac) {
            andraRaiz.setStyle("-fx-font-family: 'Inter', 'Segoe UI', sans-serif;");
        }
        AndraAjustes andraAjustes = andraContexto.getBean(AndraAjustesServicio.class).andraObtener();
        double andraAncho = andraAjustes.getAndraVentanaAncho() != null ? andraAjustes.getAndraVentanaAncho() : 1280;
        double andraAlto = andraAjustes.getAndraVentanaAlto() != null ? andraAjustes.getAndraVentanaAlto() : 800;
        Scene andraEscena = new Scene(andraRaiz, andraAncho, andraAlto);
        andraEscena.setCamera(new PerspectiveCamera());
        andraEscena.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/theme.css")).toExternalForm());
        andraVentana.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/logo-andra.png"))));
        andraVentana.setScene(andraEscena);
        andraVentana.setMinWidth(1024);
        andraVentana.setMinHeight(680);
        if (andraAjustes.getAndraVentanaX() != null && andraAjustes.getAndraVentanaY() != null && andraDentroDePantalla(andraAjustes.getAndraVentanaX(), andraAjustes.getAndraVentanaY())) {
            andraVentana.setX(andraAjustes.getAndraVentanaX());
            andraVentana.setY(andraAjustes.getAndraVentanaY());
        }
        andraVentana.setMaximized(andraAjustes.isAndraMaximizada());
        andraPrincipal = andraCargador.getController();
        andraPrincipal.andraIniciar(andraVentana);
        andraContexto.getBean(AndraConexionServicio.class).andraIniciarMonitoreo();
        andraVentana.show();
    }

    private static boolean andraDentroDePantalla(double andraPosicionX, double andraPosicionY) {
        return Screen.getScreens().stream().anyMatch(andraPantalla -> andraPantalla.getVisualBounds().contains(andraPosicionX + 40, andraPosicionY + 20));
    }

    @Override
    public void stop() {
        if (andraPrincipal != null) {
            andraPrincipal.andraGuardarVentana();
        }
        andraContexto.close();
        Platform.exit();
    }
}
