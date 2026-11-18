package pe.andra.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.DixonAdministrador;
import pe.andra.model.DixonPersona;
import pe.andra.model.DixonRol;
import pe.andra.service.DixonIdiomaServicio;
import pe.andra.service.DixonSesionServicio;
import pe.andra.ui.*;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AndraIngresoControlador implements AndraVista {

    private final DixonSesionServicio andraSesion;
    private final DixonIdiomaServicio andraIdioma;
    private final AndraNotificador andraNotificador;
    private final AndraPrincipalControlador andraPrincipal;

    @FXML
    private HBox andraRaiz;

    private DixonRol andraRol = DixonRol.ESTUDIANTE;
    private String andraError = "";

    @Override
    public void andraRefrescar() {
        andraRaiz.getChildren().setAll(andraArte(), andraFormulario());
    }

    private StackPane andraArte() {
        Image andraFoto = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/carrusel/01-bienvenida-campus.jpg")));
        ImageView andraVista = new ImageView(andraFoto);
        andraVista.setPreserveRatio(false);
        StackPane andraMarco = new StackPane(andraVista);
        andraMarco.getStyleClass().add("login-art");
        andraMarco.setMinWidth(0);
        HBox.setHgrow(andraMarco, Priority.ALWAYS);
        HBox.setMargin(andraMarco, new Insets(16));
        andraMarco.prefWidthProperty().bind(andraRaiz.widthProperty().multiply(0.52));
        andraMarco.layoutBoundsProperty().addListener((andraObservable, andraLimitesA, andraLimitesB) -> {
            double andraAncho = andraLimitesB.getWidth();
            double andraAlto = andraLimitesB.getHeight();
            if (andraAncho <= 0 || andraAlto <= 0) {
                return;
            }
            andraVista.setFitWidth(andraAncho);
            andraVista.setFitHeight(andraAlto);
            double andraEscala = Math.max(andraAncho / andraFoto.getWidth(), andraAlto / andraFoto.getHeight());
            double andraAnchoVista = andraAncho / andraEscala;
            double andraAltoVista = andraAlto / andraEscala;
            andraVista.setViewport(new Rectangle2D((andraFoto.getWidth() - andraAnchoVista) * 0.5, (andraFoto.getHeight() - andraAltoVista) * 0.45, andraAnchoVista, andraAltoVista));
            Rectangle andraRecorte = new Rectangle(andraAncho, andraAlto);
            andraRecorte.setArcWidth(48);
            andraRecorte.setArcHeight(48);
            andraMarco.setClip(andraRecorte);
        });
        VBox andraTarjeta = new VBox(AndraComponentes.andraLogo(48), AndraComponentes.andraLiteral("Andra", "h2"),
                AndraComponentes.andraEtiqueta("tus trámites más simples", "muted"));
        andraTarjeta.getStyleClass().add("login-art-txt");
        andraTarjeta.setMaxHeight(Region.USE_PREF_SIZE);
        andraTarjeta.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(andraTarjeta, Pos.BOTTOM_LEFT);
        StackPane.setMargin(andraTarjeta, new Insets(28));
        andraMarco.getChildren().add(andraTarjeta);
        return andraMarco;
    }

    private VBox andraFormulario() {
        FlowPane andraIdiomas = new FlowPane(4, 4);
        andraIdiomas.setAlignment(Pos.CENTER_RIGHT);
        ToggleGroup andraGrupoIdioma = new ToggleGroup();
        for (String[] andraTextos : DixonIdiomaServicio.DIXON_IDIOMAS) {
            ToggleButton andraBotonOpcionB = new ToggleButton(andraTextos[1]);
            andraBotonOpcionB.getStyleClass().addAll("chip", "lang");
            andraBotonOpcionB.setToggleGroup(andraGrupoIdioma);
            andraBotonOpcionB.setSelected(andraTextos[0].equals(andraIdioma.dixonObtenerIdioma()));
            andraBotonOpcionB.setOnAction(andraEvento -> {
                andraPrincipal.andraCambiarIdioma(andraTextos[0]);
                andraBotonOpcionB.setSelected(true);
            });
            andraIdiomas.getChildren().add(andraBotonOpcionB);
        }
        VBox.setMargin(andraIdiomas, new Insets(0, 0, 18, 0));

        HBox andraMarca = new HBox(10, AndraComponentes.andraLogo(40), new VBox(AndraComponentes.andraLiteral("Andra", "h4"), AndraComponentes.andraLiteral("Institución", "muted", "small")));
        andraMarca.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(andraMarca, new Insets(0, 0, 28, 0));

        Label andraTitulo = AndraComponentes.andraParrafo("Ingresa a tu cuenta", "login-titulo");
        Label andraSub = AndraComponentes.andraParrafo("Elige cómo quieres ingresar.", "muted");

        AndraSegmentado andraTipo = new AndraSegmentado(true).andraOpcion("ESTUDIANTE", "Estudiante", "fth-user", null)
                .andraOpcion("ADMINISTRADOR", "Administrador", "fth-shield", null).andraSeleccionar(andraRol.name());
        andraTipo.andraAlCambiar(andraValor -> {
            andraRol = DixonRol.valueOf(andraValor);
            andraError = "";
            andraRefrescar();
        });
        VBox.setMargin(andraTipo, new Insets(18, 0, 20, 0));

        boolean andraEstudiante = andraRol == DixonRol.ESTUDIANTE;
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<TextField> andraUsuario = andraFormulario.andraAgregar(AndraFormulario.andraTexto(andraEstudiante ? "Código universitario o DNI" : "Correo institucional", null, ""))
                .andraRequerido(andraEstudiante ? "Escribe tu código o DNI." : "Escribe tu correo institucional.");
        AndraCampo<PasswordField> andraClave = andraFormulario.andraAgregar(AndraFormulario.andraClave("Contraseña")).andraRequerido("Escribe tu contraseña.");
        VBox.setMargin(andraClave.andraNodo(), new Insets(14, 0, 0, 0));
        Label andraEtiquetaError = AndraComponentes.andraParrafo(andraError, "err");
        andraEtiquetaError.setVisible(!andraError.isBlank());
        andraEtiquetaError.setManaged(!andraError.isBlank());
        VBox.setMargin(andraEtiquetaError, new Insets(10, 0, 0, 0));
        Button andraIngresar = AndraComponentes.andraBloque(AndraComponentes.andraBoton(andraEstudiante ? "Ingresar como estudiante" : "Ingresar como administrador",
                "btn-primary", null, null));
        andraIngresar.setDefaultButton(true);
        VBox.setMargin(andraIngresar, new Insets(14, 0, 0, 0));
        andraIngresar.setOnAction(andraEvento -> {
            if (!andraFormulario.andraValidar()) {
                return;
            }
            try {
                andraSesion.dixonIngresar(andraRol, andraUsuario.andraControl().getText(), andraClave.andraControl().getText());
                andraError = "";
                AndraNavegacion.andraIr(andraRaiz, "sesion:entrar");
            } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                andraError = andraNegocioExcepcion.getMessage();
                AndraComponentes.andraFijar(andraEtiquetaError, andraError);
                andraEtiquetaError.setVisible(true);
                andraEtiquetaError.setManaged(true);
                andraClave.andraControl().clear();
                andraClave.andraControl().requestFocus();
            }
        });
        Button andraOlvido = AndraComponentes.andraEnlaceFlecha("¿Olvidaste tu contraseña?", andraEvento -> andraNotificador.andraInformacion(
                "En el sistema real recibirías un enlace en tu correo institucional. En esta demostración usa la cuenta de ejemplo."));
        andraOlvido.setGraphic(null);
        VBox.setMargin(andraOlvido, new Insets(8, 0, 0, 0));

        VBox andraDemo = new VBox();
        andraDemo.getStyleClass().add("demo-box");
        andraDemo.getChildren().add(AndraComponentes.andraEtiqueta("Cuenta de demostración", "strong"));
        if (andraEstudiante) {
            DixonPersona andraPersona = andraSesion.dixonEstudiante();
            andraDemo.getChildren().add(AndraComponentes.andraParrafo("Código " + andraPersona.getDixonCodigo() + " o DNI " + andraPersona.getDixonDni() + " · contraseña andra2026"));
        } else {
            DixonAdministrador andraAdministradorA = andraSesion.dixonAdministrador();
            andraDemo.getChildren().add(AndraComponentes.andraParrafo("Correo " + andraAdministradorA.getDixonCorreo() + " · contraseña admin2026"));
        }
        andraDemo.getChildren().add(AndraComponentes.andraBotonSm("Usar cuenta de demostración", "btn-secondary", null, andraEvento -> {
            andraUsuario.andraControl().setText(andraEstudiante ? andraSesion.dixonEstudiante().getDixonCodigo() : andraSesion.dixonAdministrador().getDixonCorreo());
            andraClave.andraControl().setText(andraEstudiante ? "andra2026" : "admin2026");
            andraIngresar.requestFocus();
        }));
        VBox.setMargin(andraDemo, new Insets(22, 0, 0, 0));
        Label andraNota = AndraComponentes.andraParrafo("Prototipo: solo funcionan las cuentas de demostración y no se envía información a ningún servidor.", "hint");
        VBox.setMargin(andraNota, new Insets(14, 0, 0, 0));

        VBox andraCaja = new VBox(andraIdiomas, andraMarca, andraTitulo, andraSub, andraTipo, andraUsuario.andraNodo(), andraClave.andraNodo(), andraEtiquetaError, andraIngresar, andraOlvido, andraDemo, andraNota);
        andraCaja.getStyleClass().add("login-box");
        andraCaja.setPrefWidth(440);
        andraCaja.setMaxWidth(440);
        VBox andraColumna = new VBox(andraCaja);
        andraColumna.setAlignment(Pos.CENTER);
        HBox.setHgrow(andraColumna, Priority.ALWAYS);
        ScrollPane andraDesplazable = new ScrollPane(andraColumna);
        andraDesplazable.setFitToWidth(true);
        andraDesplazable.setFitToHeight(true);
        andraDesplazable.getStyleClass().add("pagina-scroll");
        VBox andraEnvoltura = new VBox(andraDesplazable);
        VBox.setVgrow(andraDesplazable, Priority.ALWAYS);
        andraEnvoltura.prefWidthProperty().bind(andraRaiz.widthProperty().multiply(0.48));
        andraEnvoltura.setMinWidth(460);
        Platform.runLater(() -> andraUsuario.andraControl().requestFocus());
        return andraEnvoltura;
    }
}
