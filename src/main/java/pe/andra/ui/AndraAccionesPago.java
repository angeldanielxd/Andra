package pe.andra.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;
import pe.andra.util.DixonPortapapeles;
import pe.andra.util.DixonTarjetaJemm;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AndraAccionesPago {

    private final AndraNotificador andraNotificador;
    private final DixonPagoServicio andraPagos;
    private final DixonCatalogoServicio andraCatalogo;
    private final DixonSesionServicio andraSesion;
    private final DixonContextoServicio andraContexto;
    private final DixonConexionServicio andraConexion;
    private final DixonDocumentoGeneradoServicio andraGenerador;
    private final AndraDescargas andraDescargas;

    private Runnable andraAlCambiar = () -> {
    };

    private static final class AndraBorrador {
        DixonTramite andraTramite;
        DixonDatosSolicitante andraSol;
        String andraMotivo;
        String andraSede;
        int andraCantidad = 1;
        byte[] andraFoto;
        String andraNombreFoto;
    }

    public void andraAlCambiar(Runnable andraAccion) {
        this.andraAlCambiar = andraAccion == null ? () -> {
        } : andraAccion;
    }

    public static FlowPane andraPasos(int andraActual) {
        String[] andraNombres = {"Datos", "Orden de pago", "Pago", "Listo"};
        FlowPane andraPanelFlujo = new FlowPane();
        andraPanelFlujo.getStyleClass().add("wz-steps");
        for (int andraIndice = 0; andraIndice < andraNombres.length; andraIndice++) {
            int andraCantidad = andraIndice + 1;
            StackPane andraNum = new StackPane(andraCantidad < andraActual ? new FontIcon("fth-check") : AndraComponentes.andraLiteral(String.valueOf(andraCantidad)));
            andraNum.getStyleClass().add("wz-num");
            HBox andraPaso = new HBox(andraNum, AndraComponentes.andraEtiqueta(andraNombres[andraIndice]));
            andraPaso.getStyleClass().add("wz-step");
            if (andraCantidad < andraActual) {
                andraPaso.getStyleClass().add("done");
            } else if (andraCantidad == andraActual) {
                andraPaso.getStyleClass().add("cur");
            }
            andraPanelFlujo.getChildren().add(andraPaso);
        }
        AndraComponentes.andraAccesible(andraPanelFlujo, "Paso " + andraActual + " de 4");
        return andraPanelFlujo;
    }

    public void andraIniciar(DixonTramite andraTramite, String andraMotivo, Runnable andraAlTerminar) {
        andraAlCambiar(andraAlTerminar);
        DixonPersona andraEstudianteActual = andraSesion.dixonEstudiante();
        AndraBorrador andraBorrador = new AndraBorrador();
        andraBorrador.andraTramite = andraTramite;
        andraBorrador.andraSol = DixonDatosSolicitante.builder().dixonTipo("Estudiante").dixonNombre(andraEstudianteActual.getDixonNombre()).dixonDni(andraEstudianteActual.getDixonDni()).dixonCodigo(andraEstudianteActual.getDixonCodigo())
                .dixonEscuela(andraEstudianteActual.getDixonEscuela().getDixonCodigo()).dixonCorreo(andraEstudianteActual.getDixonCorreo()).build();
        andraBorrador.andraMotivo = andraMotivo == null || andraMotivo.isBlank() ? "Trámite personal" : andraMotivo;
        andraBorrador.andraSede = andraContexto.dixonTodasLasSedes() ? andraEstudianteActual.getDixonSede().getDixonCodigo() : andraContexto.dixonCodigoSede();
        andraPaso1(andraBorrador);
    }

    private void andraPaso1(AndraBorrador andraBorradorB) {
        DixonTramite andraTramite = andraBorradorB.andraTramite;
        DixonDatosSolicitante andraDatosSolicitante = andraBorradorB.andraSol;
        boolean andraPideCodigo = List.of("Estudiante", "Egresado").contains(andraDatosSolicitante.getDixonTipo());
        boolean andraConEscuela = List.of("Estudiante", "Egresado", "Docente").contains(andraDatosSolicitante.getDixonTipo());
        VBox andraResumenTexto = new VBox(2, AndraComponentes.andraMayus(andraTramite.getDixonCategoria(), "label-caps"), AndraComponentes.andraParrafo(andraTramite.getDixonNombre(), "strong"),
                AndraComponentes.andraParrafo(andraTramite.getDixonDescripcion(), "hint"));
        HBox.setHgrow(andraResumenTexto, Priority.ALWAYS);
        VBox andraPrecio = new VBox(AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraTramite.getDixonMonto()), "wz-price"));
        andraPrecio.setAlignment(Pos.CENTER_RIGHT);
        if (andraTramite.getDixonUnidad() != null) {
            andraPrecio.getChildren().add(AndraComponentes.andraEtiqueta(andraTramite.getDixonUnidad(), "hint"));
        }
        HBox andraResumen = new HBox(andraResumenTexto, andraPrecio);
        andraResumen.getStyleClass().add("wz-sum");

        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<ComboBox<AndraOpcion>> andraTipo = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Tipo de solicitante", AndraOpcion.andraLista(DixonCatalogoServicio.DIXON_SOLICITANTES), null, andraDatosSolicitante.getDixonTipo()));
        AndraCampo<TextField> andraNombre = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Nombres y apellidos", null, andraDatosSolicitante.getDixonNombre())).andraRequerido("Escribe tus nombres y apellidos.");
        AndraCampo<TextField> andraDni = andraFormulario.andraAgregar(AndraFormulario.andraNumero("DNI", null, andraDatosSolicitante.getDixonDni(), 8)).andraRequerido("Escribe tu DNI.")
                .andraPatron("\\d{8}", "El DNI debe tener 8 dígitos.");
        String andraEtiquetaCodigo = andraPideCodigo ? "Código universitario" : "Público en general".equals(andraDatosSolicitante.getDixonTipo())
                ? "Institución o empresa (opcional)" : "Código o área de trabajo";
        AndraCampo<TextField> andraCodigo = andraFormulario.andraAgregar(AndraFormulario.andraTexto(andraEtiquetaCodigo, null, "Público en general".equals(andraDatosSolicitante.getDixonTipo()) ? "" : andraDatosSolicitante.getDixonCodigo()));
        if (andraPideCodigo) {
            andraCodigo.andraRequerido("Escribe tu código universitario.");
        }
        List<AndraOpcion> andraEscuelas = new ArrayList<>();
        if (andraConEscuela) {
            andraCatalogo.dixonEscuelas().forEach(andraEscuelaProfesional -> andraEscuelas.add(AndraOpcion.andraDe(andraEscuelaProfesional.getDixonCodigo(), andraEscuelaProfesional.getDixonNombre())));
        } else {
            andraCatalogo.dixonDependencias().forEach(andraTexto -> andraEscuelas.add(AndraOpcion.andraDe(andraTexto, andraTexto)));
        }
        AndraCampo<ComboBox<AndraOpcion>> andraEscuela = andraFormulario.andraAgregar(AndraFormulario.andraSelector(andraConEscuela ? "Escuela profesional" : "Dependencia", andraEscuelas,
                andraConEscuela ? null : "No aplica", andraDatosSolicitante.getDixonEscuela()));
        if (andraConEscuela) {
            andraEscuela.andraRequerido("Elige tu escuela.");
        }
        AndraCampo<TextField> andraCorreo = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Correo electrónico", null, andraDatosSolicitante.getDixonCorreo())).andraRequerido("Escribe tu correo.")
                .andraPatron("[^@\\s]+@[^@\\s]+\\.[^@\\s]+", "Escribe un correo válido.");
        AndraCampo<ComboBox<AndraOpcion>> andraUso = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Uso del documento", AndraOpcion.andraLista(DixonPagoServicio.DIXON_USOS), null, andraBorradorB.andraMotivo));
        List<AndraOpcion> andraSedes = new ArrayList<>();
        andraCatalogo.dixonSedes().forEach(andraPosicionX -> andraSedes.add(AndraOpcion.andraDe(andraPosicionX.getDixonCodigo(), andraPosicionX.getDixonNombreCompleto() + " · Mesa de Partes")));
        AndraCampo<ComboBox<AndraOpcion>> andraSede = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Sede de recojo", andraSedes, null, andraBorradorB.andraSede));
        List<Node> andraNodos = new ArrayList<>(List.of(andraTipo.andraNodo(), andraNombre.andraNodo(), andraDni.andraNodo(), andraCodigo.andraNodo(), andraEscuela.andraNodo(), andraCorreo.andraNodo(),
                andraUso.andraNodo(), andraSede.andraNodo()));
        AndraCampo<TextField> andraCantidad = null;
        if (andraTramite.isDixonPorCantidad()) {
            andraCantidad = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Cantidad de cursos", null, String.valueOf(andraBorradorB.andraCantidad), 2));
            andraNodos.add(andraCantidad.andraNodo());
        }
        AndraCampo<AndraFormulario.AndraArchivo> andraFoto = null;
        if (andraTramite.dixonEsCarne()) {
            andraFoto = andraFormulario.andraAgregar(AndraFormulario.andraArchivo("Foto tamaño carné", "*.jpg *.jpeg *.png", "Fondo blanco, de frente. JPG o PNG.", andraNotificador, andraArchivo -> {
                try {
                    andraBorradorB.andraFoto = Files.readAllBytes(andraArchivo.toPath());
                    andraBorradorB.andraNombreFoto = andraArchivo.getName();
                } catch (IOException andraError) {
                    andraNotificador.andraError("No se pudo leer la foto.");
                }
            }));
            andraNodos.add(andraFoto.andraNodo());
        }
        Label andraErrorPublico = AndraComponentes.andraParrafo("", "err");
        andraErrorPublico.setVisible(false);
        andraErrorPublico.setManaged(false);
        Runnable andraRecolectar = () -> {
            andraBorradorB.andraSol = DixonDatosSolicitante.builder().dixonTipo(AndraFormulario.andraValor(andraTipo.andraControl())).dixonNombre(andraNombre.andraControl().getText().trim())
                    .dixonDni(andraDni.andraControl().getText().trim()).dixonCodigo(andraCodigo.andraControl().getText().trim())
                    .dixonEscuela(AndraFormulario.andraValor(andraEscuela.andraControl()).isBlank() ? null : AndraFormulario.andraValor(andraEscuela.andraControl()))
                    .dixonCorreo(andraCorreo.andraControl().getText().trim()).build();
            andraBorradorB.andraMotivo = AndraFormulario.andraValor(andraUso.andraControl());
            andraBorradorB.andraSede = AndraFormulario.andraValor(andraSede.andraControl());
        };
        andraTipo.andraControl().valueProperty().addListener((andraObservable, andraOpcionA, andraOpcion) -> {
            if (andraOpcion == null || andraOpcionA == null || andraOpcion.andraValor().equals(andraOpcionA.andraValor())) {
                return;
            }
            andraRecolectar.run();
            andraBorradorB.andraSol.setDixonTipo(andraOpcion.andraValor());
            if ("Público en general".equals(andraOpcion.andraValor())) {
                andraBorradorB.andraSol.setDixonCodigo("");
                andraBorradorB.andraSol.setDixonEscuela(null);
            } else if (!List.of("Estudiante", "Egresado", "Docente").contains(andraOpcion.andraValor())) {
                andraBorradorB.andraSol.setDixonEscuela(null);
            } else if (andraBorradorB.andraSol.getDixonEscuela() == null || andraCatalogo.dixonEscuela(andraBorradorB.andraSol.getDixonEscuela()).isEmpty()) {
                andraBorradorB.andraSol.setDixonEscuela(andraSesion.dixonEstudiante().getDixonEscuela().getDixonCodigo());
            }
            andraPaso1(andraBorradorB);
        });
        GridPane andraRejilla = AndraFormulario.andraRejilla(2, andraNodos.toArray(new Node[0]));
        Label andraSubtitulo = AndraComponentes.andraMayus("Datos del solicitante", "label-caps");
        Label andraAclaracion = AndraComponentes.andraEtiqueta("· se usan para generar el documento", "hint");
        HBox andraTitulo = new HBox(6, andraSubtitulo, andraAclaracion);
        andraTitulo.setAlignment(Pos.CENTER_LEFT);
        andraTitulo.setPadding(new Insets(8, 0, 0, 0));
        VBox andraCuerpo = new VBox(14, andraPasos(1), andraResumen, andraTitulo, andraRejilla, andraErrorPublico);
        Button andraSiguiente = AndraComponentes.andraBoton("Generar orden de pago", "btn-primary", "fth-arrow-right", null);
        andraSiguiente.setContentDisplay(ContentDisplay.RIGHT);
        AndraCampo<TextField> andraCantidadFinal = andraCantidad;
        AndraCampo<AndraFormulario.AndraArchivo> andraFotoFinal = andraFoto;
        andraSiguiente.setOnAction(andraEvento -> {
            if (!andraFormulario.andraValidar()) {
                return;
            }
            andraRecolectar.run();
            if (andraCantidadFinal != null) {
                String andraConsulta = andraCantidadFinal.andraControl().getText().trim();
                andraBorradorB.andraCantidad = Math.max(1, Math.min(20, andraConsulta.isEmpty() ? 1 : Integer.parseInt(andraConsulta)));
            }
            Map<String, String> andraErrores = andraPagos.dixonValidarSolicitante(andraTramite, andraBorradorB.andraSol, andraBorradorB.andraFoto != null);
            if (andraErrores.containsKey("publico")) {
                AndraComponentes.andraFijar(andraErrorPublico, andraErrores.get("publico"));
                andraErrorPublico.setVisible(true);
                andraErrorPublico.setManaged(true);
                return;
            }
            if (andraErrores.containsKey("foto") && andraFotoFinal != null) {
                andraFotoFinal.andraMostrar(andraErrores.get("foto"));
                return;
            }
            andraPaso2(andraBorradorB);
        });
        andraNotificador.andraModal("Solicitar " + andraTramite.getDixonNombre().toLowerCase(), andraCuerpo, true,
                AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()), andraSiguiente);
    }

    private void andraPaso2(AndraBorrador andraBorrador) {
        DixonTramite andraTramite = andraBorrador.andraTramite;
        List<DixonElementoPago> andraElementos = DixonPagoServicio.dixonElementosPara(andraTramite, andraBorrador.andraCantidad);
        BigDecimal andraTotal = DixonPagoServicio.dixonTotalOrden(andraElementos);
        DixonSede andraSede = andraCatalogo.dixonSede(andraBorrador.andraSede).orElseThrow();
        String andraCodigo = andraPagos.dixonSiguienteCodigoOrden();
        DixonPagoServicio.DixonRecojo andraRecojo = DixonPagoServicio.dixonRecojo(andraTramite, DixonFechas.dixonAhora());
        HBox andraRecojo2 = new HBox(new FontIcon("fth-calendar"), new VBox(2, AndraComponentes.andraEtiqueta("Recojo estimado si pagas ahora", "strong"),
                AndraComponentes.andraParrafo(andraRecojo.dixonFecha() != null ? andraRecojo.dixonTexto() + " · Mesa de Partes, " + andraSede.getDixonNombreCompleto() : andraRecojo.dixonTexto(), "small")));
        andraRecojo2.getStyleClass().add("wz-pick");
        VBox andraCuerpo = new VBox(14, andraPasos(2), AndraVistaFactura.andraCrear(andraCodigo, andraElementos, andraBorrador.andraSol, DixonFechas.dixonHoy().plusDays(3), andraSede, null, andraCatalogo), andraRecojo2);
        Button andraVolver = AndraComponentes.andraBoton("Volver", "btn-secondary", null, andraEvento -> andraPaso1(andraBorrador));
        Button andraDespues = AndraComponentes.andraBoton("Pagar después", "btn-secondary", null, andraEvento -> {
            DixonPago andraPago = andraCrear(andraBorrador);
            if (andraPago == null) {
                return;
            }
            andraNotificador.andraCerrarModal();
            andraAlCambiar.run();
            andraNotificador.andraExito("Orden " + andraPago.getDixonCodigo() + " guardada en Andra Pay. Págala antes del " + DixonFechas.dixonDmy(andraPago.getDixonVence()) + ".");
        });
        Button andraPagar = AndraComponentes.andraBoton("Pagar " + DixonMontos.dixonSoles(andraTotal), "btn-primary", "fth-credit-card", andraEvento -> {
            DixonPago andraPago = andraCrear(andraBorrador);
            if (andraPago != null) {
                andraAlCambiar.run();
                andraPantalla(andraPago.getDixonCodigo(), andraAlCambiar);
            }
        });
        andraNotificador.andraModal("Orden de pago", andraCuerpo, true, andraVolver, andraDespues, andraPagar);
    }

    private DixonPago andraCrear(AndraBorrador andraBorrador) {
        try {
            return andraPagos.dixonCrearOrden(andraBorrador.andraTramite, andraBorrador.andraSol, andraBorrador.andraCantidad, andraBorrador.andraMotivo, andraBorrador.andraSede, andraBorrador.andraFoto);
        } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
            andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            return null;
        }
    }

    public void andraSinConexion(DixonPago andraPago) {
        andraNotificador.andraModal("Necesitas conexión para pagar", AndraComponentes.andraParrafo("Los pagos con Yape, Plin o tarjeta requieren internet. Tu orden "
                + andraPago.getDixonCodigo() + " quedó guardada: págala cuando vuelva la conexión.", "muted"), false,
                AndraComponentes.andraBoton("Entendido", "btn-primary", null, andraEvento -> andraNotificador.andraCerrarModal()));
    }

    public void andraPantalla(String andraCodigo, Runnable andraAlTerminar) {
        andraAlCambiar(andraAlTerminar);
        DixonPago andraPago = andraPagos.dixonObtener(andraCodigo);
        if (andraConexion.dixonSinConexion()) {
            andraSinConexion(andraPago);
            return;
        }
        if (andraPago.dixonEsPagado()) {
            andraComprobante(andraCodigo);
            return;
        }
        new AndraPantallaPago(andraPago).andraDibujar();
    }

    private final class AndraPantallaPago {
        private final DixonPago andraPago;
        private String andraMetodo;
        private String andraSub = "qr";
        private Timeline andraReloj;
        private final StackPane andraPanel = new StackPane();

        AndraPantallaPago(DixonPago andraPago) {
            this.andraPago = andraPago;
        }

        void andraDetener() {
            if (andraReloj != null) {
                andraReloj.stop();
            }
        }

        void andraDibujar() {
            andraDetener();
            BigDecimal andraTotal = andraPago.dixonObtenerTotal();
            VBox andraResumen = new VBox(6, AndraComponentes.andraMayus(andraPago.getDixonTipo().dixonObtenerEtiqueta(), "label-caps"),
                    AndraComponentes.andraLiteralParrafo(andraPago.getDixonConcepto(), "strong"), AndraComponentes.andraLiteral(andraPago.getDixonCodigo(), "hint"));
            VBox andraElementos = new VBox(8);
            for (DixonElementoPago andraElementoPago : andraPago.getDixonElementos()) {
                HBox andraFila = new HBox(8, AndraComponentes.andraLiteralParrafo(andraElementoPago.getDixonConcepto() + (andraElementoPago.getDixonCantidad() > 1 ? " × " + andraElementoPago.getDixonCantidad() : ""), "small"),
                        AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraElementoPago.dixonObtenerImporte()), "strong", "small"));
                ((Label) andraFila.getChildren().get(2)).setMinWidth(Region.USE_PREF_SIZE);
                andraElementos.getChildren().add(andraFila);
            }
            VBox.setMargin(andraElementos, new Insets(8, 0, 0, 0));
            HBox andraTot = new HBox(AndraComponentes.andraEtiqueta("Total"), AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraTotal), "pay-total-valor"));
            andraTot.getStyleClass().add("pay-total");
            andraResumen.getChildren().addAll(andraElementos, andraTot);
            if (andraPago.getDixonVence() != null) {
                andraResumen.getChildren().add(AndraComponentes.andraEtiqueta("Vence el " + DixonFechas.dixonDmy(andraPago.getDixonVence()), "hint"));
            }
            andraResumen.getStyleClass().add("pay-sum");

            ToggleGroup andraGrupo = new ToggleGroup();
            VBox andraMetodos = new VBox(10);
            for (String[] andraTextos : new String[][]{{"yape", "Yape", "Paga con QR o código de aprobación"}, {"plin", "Plin", "Paga con QR o tu número"},
                    {"tarjeta", "Tarjeta", "Visa, Mastercard, Amex, Diners"}}) {
                StackPane andraInsignia = new StackPane("tarjeta".equals(andraTextos[0]) ? new FontIcon("fth-credit-card") : AndraComponentes.andraLiteral(andraTextos[1].substring(0, 1)));
                andraInsignia.getStyleClass().add("pm-badge");
                VBox andraTextos2 = new VBox(AndraComponentes.andraLiteral(andraTextos[1], "pm-tit"), AndraComponentes.andraEtiqueta(andraTextos[2], "pm-sub"));
                HBox andraContenido = new HBox(10, andraInsignia, andraTextos2);
                andraContenido.setAlignment(Pos.CENTER_LEFT);
                ToggleButton andraBotonOpcion = new ToggleButton();
                andraBotonOpcion.setGraphic(andraContenido);
                andraBotonOpcion.getStyleClass().addAll("pm-tile", "pm-" + andraTextos[0]);
                andraBotonOpcion.setToggleGroup(andraGrupo);
                andraBotonOpcion.setMaxWidth(Double.MAX_VALUE);
                andraBotonOpcion.setSelected(andraTextos[0].equals(andraMetodo));
                andraBotonOpcion.setOnAction(andraEvento -> {
                    andraMetodo = andraTextos[0];
                    andraSub = "qr";
                    andraDibujar();
                });
                AndraComponentes.andraAccesible(andraBotonOpcion, andraTextos[1] + ". " + andraTextos[2]);
                andraMetodos.getChildren().add(andraBotonOpcion);
            }
            andraPanel.getChildren().setAll(andraMetodo == null ? andraVacio() : andraContenidoMetodo(andraTotal));
            VBox andraDerecha = new VBox(10, AndraComponentes.andraMayus("Elige cómo pagar", "label-caps"), andraMetodos, andraPanel);
            HBox.setHgrow(andraDerecha, Priority.ALWAYS);
            HBox andraRejilla = new HBox(22, andraResumen, andraDerecha);
            andraRejilla.getStyleClass().add("pay-grid");
            VBox andraCuerpo = new VBox(14);
            if (andraPago.getDixonTramite() != null) {
                andraCuerpo.getChildren().add(andraPasos(3));
            }
            andraCuerpo.getChildren().add(andraRejilla);
            Label andraNota = AndraComponentes.andraEtiqueta("Pago simulado para demostración", "hint");
            andraNota.setGraphic(AndraComponentes.andraIcono("fth-shield"));
            HBox andraNotaCaja = new HBox(andraNota);
            andraNotaCaja.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(andraNotaCaja, Priority.ALWAYS);
            Button andraDespues = AndraComponentes.andraBoton("Pagar después", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal());
            andraNotificador.andraModal("Pagar con Andra Pay", andraCuerpo, true, this::andraDetener, andraNotaCaja, andraDespues);
        }

        private Node andraVacio() {
            VBox andraCajaVertical = new VBox(new FontIcon("fth-credit-card"), AndraComponentes.andraParrafo("Selecciona Yape, Plin o tarjeta para continuar.", "muted", "small"));
            andraCajaVertical.getStyleClass().add("pm-empty");
            return andraCajaVertical;
        }

        private Node andraContenidoMetodo(BigDecimal andraTotal) {
            if ("tarjeta".equals(andraMetodo)) {
                return andraTarjeta(andraTotal);
            }
            String andraNombre = "yape".equals(andraMetodo) ? "Yape" : "Plin";
            AndraSegmentado andraSeg = new AndraSegmentado(false).andraOpcion("qr", "Código QR", null, null)
                    .andraOpcion("code", "yape".equals(andraMetodo) ? "Código de aprobación" : "Número celular", null, null).andraSeleccionar(andraSub);
            andraSeg.andraAlCambiar(andraValor -> {
                andraSub = andraValor;
                andraDibujar();
            });
            Node andraContenido;
            if ("qr".equals(andraSub)) {
                ImageView andraImagenQr = new ImageView(DixonQrServicio.dixonImagen("ANDRA-PAY|" + andraPago.getDixonCodigo() + "|" + andraTotal.setScale(2) + "|DEMO", 336));
                andraImagenQr.setFitWidth(168);
                andraImagenQr.setFitHeight(168);
                StackPane andraMarco = new StackPane(andraImagenQr);
                andraMarco.getStyleClass().add("qr-img");
                AndraComponentes.andraAccesible(andraMarco, "Código QR de pago");
                Label andraTiempo = AndraComponentes.andraLiteral("05:00", "strong");
                int[] andraSegundos = {300};
                andraReloj = new Timeline(new KeyFrame(Duration.seconds(1), andraEvento -> {
                    andraSegundos[0]--;
                    if (andraSegundos[0] <= 0) {
                        AndraComponentes.andraFijar(andraTiempo, "vencido");
                        andraReloj.stop();
                    } else {
                        andraTiempo.setText(String.format("%02d:%02d", andraSegundos[0] / 60, andraSegundos[0] % 60));
                    }
                }));
                andraReloj.setCycleCount(Animation.INDEFINITE);
                andraReloj.play();
                Button andraListo = AndraComponentes.andraBoton("Ya realicé el pago", "btn-primary", "fth-check", andraEvento -> andraProcesar(andraMetodo));
                HBox andraVence = new HBox(4, AndraComponentes.andraEtiqueta("El código vence en", "hint"), andraTiempo);
                VBox andraTexto = new VBox(6, AndraComponentes.andraEtiqueta("Escanea con tu app " + andraNombre, "strong"),
                        new HBox(4, AndraComponentes.andraEtiqueta("Monto:", "hint"), AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraTotal), "strong"),
                                AndraComponentes.andraEtiqueta("· Destino: Andra Pay", "hint")), andraVence, andraListo);
                VBox.setMargin(andraListo, new Insets(8, 0, 0, 0));
                HBox andraCaja = new HBox(18, andraMarco, andraTexto);
                andraCaja.getStyleClass().add("qr-box");
                andraContenido = andraCaja;
            } else if ("yape".equals(andraMetodo)) {
                AndraFormulario andraFormulario = new AndraFormulario();
                AndraCampo<TextField> andraCel = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Celular Yape", null, andraSesion.dixonEstudiante().getDixonCelular().replaceAll("\\s", ""), 9))
                        .andraRequerido("Escribe tu celular.").andraPatron("9\\d{8}", "Celular de 9 dígitos que empiece con 9.");
                AndraCampo<TextField> andraCod = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Código de aprobación", "••••••", "", 6))
                        .andraRequerido("Escribe el código de 6 dígitos.").andraPatron("\\d{6}", "El código tiene 6 dígitos.");
                Button andraPagar = AndraComponentes.andraBloque(AndraComponentes.andraBoton("Pagar " + DixonMontos.dixonSoles(andraTotal) + " con Yape", "btn-primary", null, andraEvento -> {
                    if (andraFormulario.andraValidar() && DixonPagoServicio.dixonValidarYape(andraCel.andraControl().getText(), andraCod.andraControl().getText()).isEmpty()) {
                        andraProcesar("yape");
                    }
                }));
                andraPagar.setDefaultButton(true);
                andraContenido = new VBox(12, AndraComponentes.andraParrafo("Abre Yape, entra al menú y toca Código de aprobación. Tienes unos minutos para usarlo.", "hint"),
                        AndraComponentes.andraParrafo("Abre Yape › Código de aprobación", "small", "azul"),
                        AndraFormulario.andraRejilla(2, andraCel.andraNodo(), andraCod.andraNodo()), andraPagar);
            } else {
                AndraFormulario andraFormulario = new AndraFormulario();
                AndraCampo<TextField> andraCel = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Celular Plin", null, andraSesion.dixonEstudiante().getDixonCelular().replaceAll("\\s", ""), 9))
                        .andraRequerido("Escribe tu celular.").andraPatron("9\\d{8}", "Celular de 9 dígitos que empiece con 9.");
                Button andraEnviar = AndraComponentes.andraBloque(AndraComponentes.andraBoton("Enviar solicitud de cobro", "btn-primary", null, null));
                VBox andraCaja = new VBox(12, AndraComponentes.andraParrafo("Te enviaremos una solicitud de cobro a tu app Plin para que la apruebes.", "hint"), andraCel.andraNodo(), andraEnviar);
                andraEnviar.setOnAction(andraEvento -> {
                    if (!andraFormulario.andraValidar()) {
                        return;
                    }
                    ProgressIndicator andraGiro = new ProgressIndicator();
                    andraGiro.setPrefSize(38, 38);
                    Button andraAprobar = AndraComponentes.andraBoton("Simular aprobación en Plin", "btn-primary", null, andraPosicionX -> andraProcesar("plin"));
                    VBox andraEspera = new VBox(10, andraGiro, AndraComponentes.andraEtiqueta("Solicitud enviada a tu Plin", "strong"),
                            AndraComponentes.andraParrafo("Apruébala en tu app. En esta demostración puedes confirmarla aquí.", "hint"), andraAprobar);
                    andraEspera.getStyleClass().add("wait");
                    andraPanel.getChildren().setAll(andraEspera);
                    andraAprobar.requestFocus();
                });
                andraContenido = andraCaja;
            }
            VBox andraCajaVertical = new VBox(14, andraSeg, andraContenido);
            AndraMovimiento.andraSubir(andraCajaVertical);
            return andraCajaVertical;
        }

        private Node andraTarjeta(BigDecimal andraTotal) {
            AndraFormulario andraFormulario = new AndraFormulario();
            AndraCampo<TextField> andraNumero = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Número de tarjeta", "0000 0000 0000 0000", "")).andraRequerido("Escribe el número de tu tarjeta.");
            Label andraMarca = AndraComponentes.andraLiteral("—", "card-brand");
            andraNumero.andraControl().textProperty().addListener((andraObservable, andraTextoA, andraTextoB) -> {
                String andraFormato = DixonTarjetaJemm.dixonFormatear(andraTextoB);
                if (!andraFormato.equals(andraTextoB)) {
                    javafx.application.Platform.runLater(() -> {
                        andraNumero.andraControl().setText(andraFormato);
                        andraNumero.andraControl().positionCaret(andraFormato.length());
                    });
                }
                DixonMetodoPago andraMetodoPago = DixonTarjetaJemm.dixonMarca(andraTextoB);
                andraMarca.setText(andraMetodoPago == null ? "—" : andraMetodoPago.dixonObtenerEtiqueta());
            });
            HBox andraFilaNumero = new HBox(8, andraNumero.andraNodo(), andraMarca);
            HBox.setHgrow(andraNumero.andraNodo(), Priority.ALWAYS);
            andraFilaNumero.setAlignment(Pos.CENTER_LEFT);
            AndraCampo<TextField> andraTitular = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Nombre del titular", null, andraSesion.dixonEstudiante().getDixonNombre().toUpperCase()))
                    .andraRequerido("Escribe el nombre como aparece en la tarjeta.");
            AndraCampo<TextField> andraVence = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Vencimiento", "MM/AA", "")).andraRequerido("Escribe el vencimiento.");
            andraVence.andraControl().textProperty().addListener((andraObservable, andraTextoA, andraTextoB) -> {
                String andraFormato = DixonTarjetaJemm.dixonFormatearVencimiento(andraTextoB);
                if (!andraFormato.equals(andraTextoB) && !(andraTextoA != null && andraTextoA.length() > andraTextoB.length())) {
                    javafx.application.Platform.runLater(() -> {
                        andraVence.andraControl().setText(andraFormato);
                        andraVence.andraControl().positionCaret(andraFormato.length());
                    });
                }
            });
            AndraCampo<TextField> andraCvv = andraFormulario.andraAgregar(AndraFormulario.andraNumero("CVV", "123", "", 4)).andraRequerido("Escribe el CVV.");
            AndraCampo<TextField> andraCorreo = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Correo para el comprobante", null, andraSesion.dixonEstudiante().getDixonCorreo()))
                    .andraRequerido("Escribe un correo.");
            Button andraPagar = AndraComponentes.andraBloque(AndraComponentes.andraBoton("Pagar " + DixonMontos.dixonSoles(andraTotal), "btn-primary", "fth-lock", null));
            andraPagar.setDefaultButton(true);
            andraPagar.setOnAction(andraEvento -> {
                if (!andraFormulario.andraValidar()) {
                    return;
                }
                Map<String, String> andraErrores = DixonPagoServicio.dixonValidarTarjeta(andraNumero.andraControl().getText(), andraTitular.andraControl().getText(),
                        andraVence.andraControl().getText(), andraCvv.andraControl().getText(), andraCorreo.andraControl().getText(), YearMonth.now(DixonFechas.dixonReloj()));
                if (andraErrores.containsKey("numero")) {
                    andraNumero.andraMostrar(andraErrores.get("numero"));
                    andraNumero.andraControl().requestFocus();
                    return;
                }
                if (andraErrores.containsKey("vencimiento")) {
                    andraVence.andraMostrar(andraErrores.get("vencimiento"));
                    andraVence.andraControl().requestFocus();
                    return;
                }
                if (andraErrores.containsKey("cvv")) {
                    andraCvv.andraMostrar(andraErrores.get("cvv"));
                    andraCvv.andraControl().requestFocus();
                    return;
                }
                DixonMetodoPago andraMetodoPago = DixonTarjetaJemm.dixonMarca(andraNumero.andraControl().getText());
                andraProcesar(andraMetodoPago.name().toLowerCase());
            });
            Label andraAyuda = AndraComponentes.andraParrafo("Prueba con 4111 1111 1111 1111. No se guardan datos de tarjeta.", "hint");
            andraAyuda.setAlignment(Pos.CENTER);
            andraAyuda.setMaxWidth(Double.MAX_VALUE);
            VBox andraCajaVertical = new VBox(12, andraFilaNumero, andraTitular.andraNodo(), AndraFormulario.andraRejilla(2, andraVence.andraNodo(), andraCvv.andraNodo()), andraCorreo.andraNodo(), andraPagar, andraAyuda);
            AndraMovimiento.andraSubir(andraCajaVertical);
            return andraCajaVertical;
        }

        private void andraProcesar(String andraClave) {
            andraDetener();
            ProgressIndicator andraGiro = new ProgressIndicator();
            andraGiro.setPrefSize(38, 38);
            VBox andraEspera = new VBox(10, andraGiro, AndraComponentes.andraEtiqueta("Procesando tu pago…", "strong"), AndraComponentes.andraEtiqueta("No cierres esta ventana.", "hint"));
            andraEspera.getStyleClass().add("wait");
            andraPanel.getChildren().setAll(andraEspera);
            PauseTransition andraPausa = new PauseTransition(Duration.millis(1400));
            andraPausa.setOnFinished(andraEvento -> {
                try {
                    andraPagos.dixonCompletar(andraPago.getDixonCodigo(), DixonMetodoPago.dixonDesdeClave(andraClave));
                    andraAlCambiar.run();
                    andraExito(andraPago.getDixonCodigo());
                } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                    andraNotificador.andraCerrarModal();
                    if (andraConexion.dixonSinConexion()) {
                        andraSinConexion(andraPago);
                    } else {
                        andraNotificador.andraError(andraNegocioExcepcion.getMessage());
                    }
                }
            });
            andraPausa.play();
        }
    }

    public void andraExito(String andraCodigo) {
        DixonPago andraPago = andraPagos.dixonObtener(andraCodigo);
        StackPane andraVerificacion = new StackPane(new FontIcon("fth-check"));
        andraVerificacion.getStyleClass().add("ok-check");
        if (!AndraMovimiento.andraReducido()) {
            andraVerificacion.setScaleX(0.4);
            andraVerificacion.setScaleY(0.4);
            ScaleTransition andraEscala = new ScaleTransition(Duration.millis(500), andraVerificacion);
            andraEscala.setToX(1);
            andraEscala.setToY(1);
            andraEscala.setInterpolator(javafx.animation.Interpolator.SPLINE(0.34, 1.56, 0.64, 1));
            andraEscala.play();
        }
        VBox andraPortada = new VBox(6, andraVerificacion, AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()), "ok-monto"), AndraComponentes.andraLiteralParrafo(andraPago.getDixonConcepto()));
        andraPortada.getStyleClass().add("ok-hero");
        Object andraFilaFinal;
        String andraEtiquetaFinal;
        if (andraPago.getDixonTipo() == DixonTipoPago.MATRICULA) {
            andraEtiquetaFinal = "Matrícula";
            andraFilaFinal = AndraComponentes.andraPildora("Confirmada", "success");
        } else {
            andraEtiquetaFinal = "Recojo";
            VBox andraCajaVertical = new VBox(2, AndraComponentes.andraParrafo(andraPago.getDixonRecojoTexto(), "dd"));
            if (andraPago.getDixonRecojoFecha() != null) {
                andraCajaVertical.getChildren().add(AndraComponentes.andraParrafo("Mesa de Partes · " + andraPago.getDixonSede().getDixonNombreCompleto() + ". Lleva tu DNI.", "hint"));
            }
            andraFilaFinal = andraCajaVertical;
        }
        javafx.scene.layout.GridPane andraParesClaveValor = AndraComponentes.andraKv("Método", andraPago.getDixonMetodo().dixonObtenerEtiqueta(), "N.° de operación", AndraComponentes.andraLiteral(andraPago.getDixonOperacion(), "dd"),
                "Comprobante", AndraComponentes.andraLiteral(andraPago.getDixonBoleta(), "dd"), "Fecha y hora", AndraComponentes.andraLiteral(DixonFechas.dixonDmyHm(andraPago.getDixonPagado()), "dd"),
                andraEtiquetaFinal, andraFilaFinal);
        andraParesClaveValor.setMaxWidth(520);
        VBox andraCuerpo = new VBox(18);
        if (andraPago.getDixonTramite() != null) {
            andraCuerpo.getChildren().add(andraPasos(4));
        }
        andraCuerpo.getChildren().addAll(andraPortada, andraParesClaveValor);
        andraCuerpo.setAlignment(Pos.TOP_CENTER);
        List<Node> andraPie = new ArrayList<>();
        andraPie.add(AndraComponentes.andraBoton("Ver comprobante", "btn-secondary", "fth-file", andraEvento -> andraComprobante(andraCodigo)));
        if (andraPago.getDixonTramite() != null && andraPago.getDixonTramite().dixonGeneraDocumento()) {
            andraPie.add(AndraComponentes.andraBoton("Ver documento", "btn-secondary", "fth-file-text", andraEvento -> andraDocumento(andraCodigo)));
        }
        andraPie.add(AndraComponentes.andraBoton("Listo", "btn-primary", null, andraEvento -> {
            andraNotificador.andraCerrarModal();
            andraAlCambiar.run();
        }));
        andraNotificador.andraModal("Pago confirmado", andraCuerpo, true, andraPie.toArray(new Node[0]));
    }

    public void andraComprobante(String andraCodigo) {
        DixonPago andraPago = andraPagos.dixonObtener(andraCodigo);
        StackPane andraHoja = AndraVistaFactura.andraCrear(andraPago.getDixonCodigo(), andraPago.getDixonElementos(), andraGenerador.dixonSolicitante(andraPago), andraPago.getDixonVence(), andraPago.getDixonSede(), andraPago, andraCatalogo);
        andraNotificador.andraModal("Comprobante de pago", andraHoja, true,
                AndraComponentes.andraBoton("Copiar N.° de operación", "btn-secondary", null, andraEvento -> {
                    DixonPortapapeles.dixonCopiar(andraPago.getDixonOperacion());
                    andraNotificador.andraExito("Número copiado.");
                }),
                AndraComponentes.andraBoton("Descargar", "btn-primary", "fth-download", andraEvento -> andraDescargas.andraGuardarPdf("Comprobante " + andraPago.getDixonBoleta(),
                        () -> andraGenerador.dixonPdfComprobante(andraPago))));
    }

    public void andraDocumento(String andraCodigo) {
        DixonPago andraPago = andraPagos.dixonObtener(andraCodigo);
        VBox andraCuerpo = new VBox(12, AndraComponentes.andraParrafo("Generado automáticamente con los datos del solicitante. Documento de demostración sin valor oficial.", "hint"),
                AndraVistaDocumento.andraCrear(andraGenerador.dixonConstruir(andraPago)));
        andraNotificador.andraModal("Documento generado", andraCuerpo, true,
                AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Descargar PDF", "btn-primary", "fth-download", andraEvento -> andraDescargas.andraGuardarPdf(andraPago.getDixonConcepto() + " " + (andraPago.getDixonBoleta() == null ? "" : andraPago.getDixonBoleta()),
                        () -> andraGenerador.dixonPdfDocumento(andraPago))));
    }

    public void andraDetalle(String andraCodigo, Runnable andraAlTerminar) {
        andraAlCambiar(andraAlTerminar);
        DixonPago andraPago = andraPagos.dixonObtener(andraCodigo);
        if (!andraPago.dixonEsPendiente()) {
            andraComprobante(andraCodigo);
            return;
        }
        FlowPane andraMetodos = new FlowPane(6, 6, AndraComponentes.andraEtiqueta("Puedes pagar con", "hint"), AndraComponentes.andraLiteral("Yape", "pm-chip", "y"),
                AndraComponentes.andraLiteral("Plin", "pm-chip", "p"), AndraComponentes.andraLiteral("Visa", "pm-chip"), AndraComponentes.andraLiteral("Mastercard", "pm-chip"));
        andraMetodos.setAlignment(Pos.CENTER);
        VBox andraCuerpo = new VBox(14, AndraVistaFactura.andraCrear(andraPago.getDixonCodigo(), andraPago.getDixonElementos(), andraGenerador.dixonSolicitante(andraPago), andraPago.getDixonVence(), andraPago.getDixonSede(), null, andraCatalogo), andraMetodos);
        andraNotificador.andraModal("Detalle del derecho de pago", andraCuerpo, true,
                AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Pagar " + DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()), "btn-primary", "fth-credit-card", andraEvento -> andraPantalla(andraCodigo, andraAlCambiar)));
    }

    public void andraNuevoPago(Runnable andraAlTerminar) {
        andraAlCambiar(andraAlTerminar);
        VBox andraLista = new VBox(8);
        for (DixonTramite andraTramite : andraCatalogo.dixonTramites()) {
            VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo(andraTramite.getDixonNombre(), "strong"), AndraComponentes.andraEtiqueta(andraTramite.getDixonCategoria() + " · " + andraTramite.dixonObtenerTextoEntrega(), "hint"));
            HBox.setHgrow(andraTextos, Priority.ALWAYS);
            HBox andraContenido = new HBox(10, andraTextos, AndraComponentes.andraLiteral(DixonMontos.dixonSoles(andraTramite.getDixonMonto()), "strong"));
            andraContenido.setAlignment(Pos.CENTER_LEFT);
            Button andraBoton = new Button();
            andraBoton.setGraphic(andraContenido);
            andraBoton.getStyleClass().add("tupa-pick");
            andraBoton.setMaxWidth(Double.MAX_VALUE);
            andraContenido.prefWidthProperty().bind(andraBoton.widthProperty().subtract(30));
            andraBoton.setOnAction(andraEvento -> andraIniciar(andraTramite, null, andraAlCambiar));
            andraLista.getChildren().add(andraBoton);
        }
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Elige el documento o trámite. Generaremos la orden con tus datos.", "muted", "small"), andraLista);
        andraNotificador.andraModal("Nuevo pago", andraCuerpo, true);
    }
}
