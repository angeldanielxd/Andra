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
import pe.andra.model.DixonDocumento;
import pe.andra.model.DixonDocumentoPersonal;
import pe.andra.model.DixonTipoFavorito;
import pe.andra.service.DixonCatalogoServicio;
import pe.andra.service.DixonDocumentoGeneradoServicio;
import pe.andra.service.DixonFavoritoServicio;
import pe.andra.service.DixonSesionServicio;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonPortapapeles;

@Component
@RequiredArgsConstructor
public class AndraAccionesDocumento {

    private final AndraNotificador andraNotificador;
    private final AndraDescargas andraDescargas;
    private final DixonDocumentoGeneradoServicio andraGenerador;
    private final DixonFavoritoServicio andraFavoritos;
    private final DixonCatalogoServicio andraCatalogo;
    private final DixonSesionServicio andraSesion;
    private final AndraDatosInstitucion andraInstitucion;

    public String andraMeta(DixonDocumento andraDocumento) {
        return andraDocumento.getDixonTipo() + " · " + andraCatalogo.dixonEtiquetaDependencia(andraDocumento.getDixonDependencia()) + " · " + DixonFechas.dixonDmy(andraDocumento.getDixonFecha()) + " · "
                + andraDocumento.getDixonPaginas() + " página" + (andraDocumento.getDixonPaginas() > 1 ? "s" : "");
    }

    public HBox andraTarjeta(DixonDocumento andraDocumento, Runnable andraAlCambiar) {
        VBox andraMiniatura = new VBox();
        andraMiniatura.getStyleClass().add("doc-thumb");
        for (double andraAncho : new double[]{1, 0.7, 1, 0.5}) {
            Region andraLinea = new Region();
            andraLinea.getStyleClass().add("linea");
            andraLinea.setMaxWidth(66 * andraAncho);
            andraMiniatura.getChildren().add(andraLinea);
        }
        andraMiniatura.getChildren().add(AndraComponentes.andraLiteral("PDF", "pdf"));
        Button andraVista = AndraComponentes.andraBotonSm("Vista previa", "btn-secondary", "fth-eye", andraEvento -> andraVistaPrevia(andraDocumento.getDixonNombre(), andraMeta(andraDocumento)));
        Button andraBajar = AndraComponentes.andraBotonSm("Descargar", "btn-secondary", "fth-download", andraEvento -> andraDescargar(andraDocumento.getDixonNombre(), andraMeta(andraDocumento)));
        Button andraCompartir = AndraComponentes.andraBotonSm("Compartir", "btn-tertiary", "fth-link", andraEvento -> andraCompartir(andraInstitucion.getAndraUrlDocumentos() + "/documentos/" + andraDocumento.getDixonCodigo()));
        FlowPane andraAcciones = new FlowPane(andraVista, andraBajar, andraCompartir);
        andraAcciones.getStyleClass().add("doc-actions");
        VBox andraCuerpo = new VBox(4, AndraComponentes.andraMayus(andraDocumento.getDixonTipo(), "label-caps"), AndraComponentes.andraParrafo(andraDocumento.getDixonNombre(), "h3", "sm"),
                AndraComponentes.andraParrafo(andraDocumento.getDixonDescripcion(), "muted", "small"),
                AndraComponentes.andraLiteralParrafo(andraCatalogo.dixonEtiquetaDependencia(andraDocumento.getDixonDependencia()) + " · " + DixonFechas.dixonDmy(andraDocumento.getDixonFecha()), "hint"), andraAcciones);
        HBox.setHgrow(andraCuerpo, Priority.ALWAYS);
        boolean andraFav = andraFavoritos.dixonEs(DixonTipoFavorito.DOCUMENTO, andraDocumento.getDixonCodigo());
        Button andraEstrella = AndraComponentes.andraEstrella(andraFav, andraDocumento.getDixonNombre(), () -> {
            boolean andraAhora = andraFavoritos.dixonAlternar(DixonTipoFavorito.DOCUMENTO, andraDocumento.getDixonCodigo());
            andraNotificador.andraExito(andraAhora ? "Agregado a favoritos." : "Quitado de favoritos.");
            andraAlCambiar.run();
        });
        andraEstrella.getStyleClass().add("plano");
        HBox andraTarjeta = new HBox(16, andraMiniatura, andraCuerpo, andraEstrella);
        andraTarjeta.getStyleClass().add("doc-card");
        return andraTarjeta;
    }

    public HBox andraFilaPersonal(DixonDocumentoPersonal andraDocumentoPersonal, Runnable andraAlCambiar) {
        HBox andraPrincipal = new HBox(12, AndraComponentes.andraIcono("fth-file-text", "doc-icono"), AndraComponentes.andraLiteral(andraDocumentoPersonal.getDixonNombre(), "row-title"));
        andraPrincipal.setAlignment(Pos.CENTER_LEFT);
        String andraMeta = "Emitido a nombre de " + andraSesion.dixonEstudiante().getDixonNombre() + " · Código " + andraSesion.dixonEstudiante().getDixonCodigo();
        boolean andraFav = andraFavoritos.dixonEs(DixonTipoFavorito.MI_DOCUMENTO, andraDocumentoPersonal.getDixonCodigo());
        Button andraEstrella = AndraComponentes.andraEstrella(andraFav, andraDocumentoPersonal.getDixonNombre(), () -> {
            boolean andraAhora = andraFavoritos.dixonAlternar(DixonTipoFavorito.MI_DOCUMENTO, andraDocumentoPersonal.getDixonCodigo());
            andraNotificador.andraExito(andraAhora ? "Agregado a favoritos." : "Quitado de favoritos.");
            andraAlCambiar.run();
        });
        andraEstrella.getStyleClass().add("plano");
        return AndraComponentes.andraFila(andraPrincipal,
                AndraComponentes.andraBotonSm("Compartir", "btn-tertiary", "fth-link", andraEvento -> andraCompartir(andraInstitucion.getAndraUrlDocumentos() + "/mis-documentos/" + andraDocumentoPersonal.getDixonCodigo())),
                AndraComponentes.andraBotonSm("Descargar", "btn-secondary", "fth-download", andraEvento -> andraDescargar(andraDocumentoPersonal.getDixonNombre(), andraMeta)),
                AndraComponentes.andraBotonSm("Vista previa", "btn-secondary", "fth-eye", andraEvento -> andraVistaPrevia(andraDocumentoPersonal.getDixonNombre(), andraMeta)), andraEstrella);
    }

    public void andraVistaPrevia(String andraNombre, String andraMeta) {
        VBox andraHoja = new VBox(10);
        andraHoja.getStyleClass().add("vista-doc");
        HBox andraArriba = new HBox(AndraComponentes.andraLiteral("Andra", "strong"), AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral("PDF", "tag"));
        Region andraSep = new Region();
        andraSep.getStyleClass().add("vista-sep");
        andraHoja.getChildren().addAll(andraArriba, andraSep, AndraComponentes.andraLiteralParrafo(andraNombre, "h3"));
        if (andraMeta != null) {
            andraHoja.getChildren().add(AndraComponentes.andraParrafo(andraMeta, "muted", "small"));
        }
        for (double andraAncho : new double[]{1, 0.94, 0.98, 0.72, 0, 1, 0.88, 0.96, 0.6}) {
            Region andraLinea = new Region();
            if (andraAncho > 0) {
                andraLinea.getStyleClass().add("linea");
                andraLinea.maxWidthProperty().bind(andraHoja.widthProperty().subtract(56).multiply(andraAncho));
            } else {
                andraLinea.setMinHeight(8);
            }
            andraHoja.getChildren().add(andraLinea);
        }
        VBox andraCuerpo = new VBox(12, andraHoja, AndraComponentes.andraParrafo("Vista previa simulada. En el sistema real aquí se mostraría el PDF.", "hint"));
        andraNotificador.andraModal(andraNombre, andraCuerpo, true, AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Descargar", "btn-primary", "fth-download", andraEvento -> andraDescargar(andraNombre, andraMeta)));
    }

    public void andraDescargar(String andraNombre, String andraMeta) {
        andraDescargas.andraGuardarPdf(andraNombre, () -> andraGenerador.dixonPdfSimple(andraNombre, andraMeta));
    }

    public void andraCompartir(String andraUrl) {
        DixonPortapapeles.dixonCopiar(andraUrl);
        andraNotificador.andraExito("Enlace del documento copiado.");
    }

    public static Label andraIcono(String andraLiteral) {
        Label andraEtiqueta = new Label(null, new FontIcon(andraLiteral));
        andraEtiqueta.setPadding(new Insets(0));
        return andraEtiqueta;
    }
}
