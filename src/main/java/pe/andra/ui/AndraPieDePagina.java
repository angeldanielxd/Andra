package pe.andra.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.config.AndraDatosInstitucion;
import pe.andra.model.AndraRol;
import pe.andra.service.AndraSesionServicio;
import pe.andra.util.AndraPortapapeles;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraPieDePagina {

    private final AndraEnlaces andraEnlaces;
    private final AndraNotificador andraNotificador;
    private final AndraDatosInstitucion andraInstitucion;
    private final AndraSesionServicio andraSesion;

    public VBox andraCrear() {
        AndraRol andraRol = andraSesion.andraRol();
        List<AndraSesionServicio.AndraRuta> andraRutas = AndraSesionServicio.andraRutas(andraRol);
        int andraCorte = andraRol == AndraRol.ADMINISTRADOR ? 3 : 4;

        VBox andraMarca = new VBox(2, AndraComponentes.andraLiteral("Andra", "brand-name"), AndraComponentes.andraLiteral("Institución", "brand-sub"),
                AndraComponentes.andraLiteral("tus trámites más simples", "brand-sub"));
        HBox andraMarcaFila = new HBox(10, AndraComponentes.andraLogo(34), andraMarca);
        andraMarcaFila.setAlignment(Pos.TOP_LEFT);
        Button andraYoutube = andraSocial("fth-youtube", "YouTube de la institución (abre en el navegador)", () -> andraEnlaces.andraAbrir(andraInstitucion.getAndraUrlYoutube()));
        Button andraFacebook = andraSocial("fth-facebook", "Facebook de la institución (abre en el navegador)", () -> andraEnlaces.andraAbrir(andraInstitucion.getAndraUrlFacebook()));
        Button andraTelefono = andraSocial("fth-phone", "Ver número de la central telefónica", this::andraModalTelefono);
        Label andraNumero = AndraComponentes.andraLiteral(andraInstitucion.getAndraTelefono(), "foot-phone");
        HBox andraRedes = new HBox(andraYoutube, andraFacebook, andraTelefono, andraNumero);
        andraRedes.getStyleClass().add("socials");
        VBox andraColMarca = new VBox(andraMarcaFila, andraRedes);

        GridPane andraRejilla = new GridPane();
        andraRejilla.getStyleClass().add("pie-grid");
        int andraColumnas = andraRol == AndraRol.ADMINISTRADOR ? 3 : 4;
        for (int andraIndice = 0; andraIndice < andraColumnas; andraIndice++) {
            ColumnConstraints andraRestriccionColumna = new ColumnConstraints();
            andraRestriccionColumna.setPercentWidth(andraIndice == 0 ? 34 : 66.0 / (andraColumnas - 1));
            andraRejilla.getColumnConstraints().add(andraRestriccionColumna);
        }
        andraRejilla.add(andraColMarca, 0, 0);
        andraRejilla.add(andraColumna("Servicios", andraRutas.subList(0, Math.min(andraCorte, andraRutas.size()))), 1, 0);
        andraRejilla.add(andraColumna("Seguimiento", andraRutas.subList(Math.min(andraCorte, andraRutas.size()), andraRutas.size())), 2, 0);
        if (andraRol != AndraRol.ADMINISTRADOR) {
            VBox andraSoporte = new VBox(AndraComponentes.andraEtiqueta("Soporte", "pie-col-titulo"), andraEnlace("Centro de ayuda", "ayuda"),
                    andraEnlace("Contacto", "contacto"));
            andraSoporte.getStyleClass().add("pie-col");
            andraRejilla.add(andraSoporte, 3, 0);
        }
        andraRejilla.setMaxWidth(1168);
        HBox andraAbajo = new HBox(AndraComponentes.andraEtiqueta("© 2026 Andra"));
        andraAbajo.getStyleClass().add("pie-bottom");
        andraAbajo.setMaxWidth(1120);
        VBox andraInterior = new VBox(andraRejilla, andraAbajo);
        andraInterior.setAlignment(Pos.TOP_CENTER);
        andraInterior.setMaxWidth(1168);
        VBox andraPie = new VBox(andraInterior);
        andraPie.setAlignment(Pos.TOP_CENTER);
        andraPie.getStyleClass().add("pie");
        VBox.setMargin(andraPie, new Insets(48, 0, 0, 0));
        andraPie.setMaxWidth(Double.MAX_VALUE);
        return andraPie;
    }

    private VBox andraColumna(String andraTitulo, List<AndraSesionServicio.AndraRuta> andraRutas) {
        VBox andraCajaVertical = new VBox(AndraComponentes.andraEtiqueta(andraTitulo, "pie-col-titulo"));
        andraCajaVertical.getStyleClass().add("pie-col");
        andraRutas.forEach(andraRuta -> andraCajaVertical.getChildren().add(andraEnlace(andraRuta.andraEtiqueta(), andraRuta.andraClave())));
        return andraCajaVertical;
    }

    private Label andraEnlace(String andraTexto, String andraRuta) {
        Label andraEtiqueta = AndraComponentes.andraEtiqueta(andraTexto, "pie-link");
        andraEtiqueta.setFocusTraversable(true);
        andraEtiqueta.setOnMouseClicked(andraEvento -> AndraNavegacion.andraIr(andraEtiqueta, andraRuta));
        andraEtiqueta.setOnKeyPressed(andraEvento -> {
            if (andraEvento.getCode() == javafx.scene.input.KeyCode.ENTER) {
                AndraNavegacion.andraIr(andraEtiqueta, andraRuta);
            }
        });
        return andraEtiqueta;
    }

    private Button andraSocial(String andraIcono, String andraDescripcion, Runnable andraAccion) {
        Button andraBoton = new Button(null, new FontIcon(andraIcono));
        andraBoton.getStyleClass().add("social");
        AndraComponentes.andraAccesible(andraBoton, andraDescripcion);
        AndraComponentes.andraAyuda(andraBoton, andraDescripcion);
        andraBoton.setOnAction(andraEvento -> andraAccion.run());
        return andraBoton;
    }

    public void andraModalTelefono() {
        StackPane andraIcono = new StackPane(new FontIcon("fth-phone"));
        andraIcono.getStyleClass().add("icono-circulo");
        Label andraNombre = AndraComponentes.andraMayus(andraInstitucion.getAndraNombre(), "label-caps");
        Label andraNumero = AndraComponentes.andraLiteral(andraInstitucion.getAndraTelefono(), "dato-grande");
        Label andraHorario = AndraComponentes.andraEtiqueta("Lunes a viernes de 08:30 a 18:00 hrs", "muted", "small");
        VBox andraCuerpo = new VBox(8, andraIcono, andraNombre, andraNumero, andraHorario);
        andraCuerpo.setAlignment(Pos.CENTER);
        andraCuerpo.setPadding(new Insets(8, 0, 4, 0));
        andraNotificador.andraModal("Central telefónica", andraCuerpo, false,
                AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Copiar número", "btn-primary", null, andraEvento -> {
                    AndraPortapapeles.andraCopiar(andraInstitucion.getAndraTelefono());
                    andraNotificador.andraExito("Número copiado.");
                }));
    }
}
