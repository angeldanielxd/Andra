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
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;
import pe.andra.util.AndraPortapapeles;
import pe.andra.util.AndraTarjetaJemm;

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
    private final AndraPagoServicio andraPagos;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraSesionServicio andraSesion;
    private final AndraContextoServicio andraContexto;
    private final AndraConexionServicio andraConexion;
    private final AndraDocumentoGeneradoServicio andraGenerador;
    private final AndraDescargas andraDescargas;

    private Runnable andraAlCambiar = () -> {
    };

    private static final class AndraBorrador {
        AndraTramite andraTramite;
        AndraDatosSolicitante andraSol;
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

    public void andraIniciar(AndraTramite andraTramite, String andraMotivo, Runnable andraAlTerminar) {
        andraAlCambiar(andraAlTerminar);
        AndraPersona andraEstudianteActual = andraSesion.andraEstudiante();
        AndraBorrador andraBorrador = new AndraBorrador();
        andraBorrador.andraTramite = andraTramite;
        andraBorrador.andraSol = AndraDatosSolicitante.builder().andraTipo("Estudiante").andraNombre(andraEstudianteActual.getAndraNombre()).andraDni(andraEstudianteActual.getAndraDni()).andraCodigo(andraEstudianteActual.getAndraCodigo())
                .andraEscuela(andraEstudianteActual.getAndraEscuela().getAndraCodigo()).andraCorreo(andraEstudianteActual.getAndraCorreo()).build();
        andraBorrador.andraMotivo = andraMotivo == null || andraMotivo.isBlank() ? "Trámite personal" : andraMotivo;
        andraBorrador.andraSede = andraContexto.andraTodasLasSedes() ? andraEstudianteActual.getAndraSede().getAndraCodigo() : andraContexto.andraCodigoSede();
        andraPaso1(andraBorrador);
    }

    private void andraPaso1(AndraBorrador andraBorradorB) {
        AndraTramite andraTramite = andraBorradorB.andraTramite;
        AndraDatosSolicitante andraDatosSolicitante = andraBorradorB.andraSol;
        boolean andraPideCodigo = List.of("Estudiante", "Egresado").contains(andraDatosSolicitante.getAndraTipo());
        boolean andraConEscuela = List.of("Estudiante", "Egresado", "Docente").contains(andraDatosSolicitante.getAndraTipo());
        VBox andraResumenTexto = new VBox(2, AndraComponentes.andraMayus(andraTramite.getAndraCategoria(), "label-caps"), AndraComponentes.andraParrafo(andraTramite.getAndraNombre(), "strong"),
                AndraComponentes.andraParrafo(andraTramite.getAndraDescripcion(), "hint"));
        HBox.setHgrow(andraResumenTexto, Priority.ALWAYS);
        VBox andraPrecio = new VBox(AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraTramite.getAndraMonto()), "wz-price"));
        andraPrecio.setAlignment(Pos.CENTER_RIGHT);
        if (andraTramite.getAndraUnidad() != null) {
            andraPrecio.getChildren().add(AndraComponentes.andraEtiqueta(andraTramite.getAndraUnidad(), "hint"));
        }
        HBox andraResumen = new HBox(andraResumenTexto, andraPrecio);
        andraResumen.getStyleClass().add("wz-sum");

        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<ComboBox<AndraOpcion>> andraTipo = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Tipo de solicitante", AndraOpcion.andraLista(AndraCatalogoServicio.ANDRA_SOLICITANTES), null, andraDatosSolicitante.getAndraTipo()));
        AndraCampo<TextField> andraNombre = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Nombres y apellidos", null, andraDatosSolicitante.getAndraNombre())).andraRequerido("Escribe tus nombres y apellidos.");
        AndraCampo<TextField> andraDni = andraFormulario.andraAgregar(AndraFormulario.andraNumero("DNI", null, andraDatosSolicitante.getAndraDni(), 8)).andraRequerido("Escribe tu DNI.")
                .andraPatron("\\d{8}", "El DNI debe tener 8 dígitos.");
        String andraEtiquetaCodigo = andraPideCodigo ? "Código universitario" : "Público en general".equals(andraDatosSolicitante.getAndraTipo())
                ? "Institución o empresa (opcional)" : "Código o área de trabajo";
        AndraCampo<TextField> andraCodigo = andraFormulario.andraAgregar(AndraFormulario.andraTexto(andraEtiquetaCodigo, null, "Público en general".equals(andraDatosSolicitante.getAndraTipo()) ? "" : andraDatosSolicitante.getAndraCodigo()));
        if (andraPideCodigo) {
            andraCodigo.andraRequerido("Escribe tu código universitario.");
        }
        List<AndraOpcion> andraEscuelas = new ArrayList<>();
        if (andraConEscuela) {
            andraCatalogo.andraEscuelas().forEach(andraEscuelaProfesional -> andraEscuelas.add(AndraOpcion.andraDe(andraEscuelaProfesional.getAndraCodigo(), andraEscuelaProfesional.getAndraNombre())));
        } else {
            andraCatalogo.andraDependencias().forEach(andraTexto -> andraEscuelas.add(AndraOpcion.andraDe(andraTexto, andraTexto)));
        }
        AndraCampo<ComboBox<AndraOpcion>> andraEscuela = andraFormulario.andraAgregar(AndraFormulario.andraSelector(andraConEscuela ? "Escuela profesional" : "Dependencia", andraEscuelas,
                andraConEscuela ? null : "No aplica", andraDatosSolicitante.getAndraEscuela()));
        if (andraConEscuela) {
            andraEscuela.andraRequerido("Elige tu escuela.");
        }
        AndraCampo<TextField> andraCorreo = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Correo electrónico", null, andraDatosSolicitante.getAndraCorreo())).andraRequerido("Escribe tu correo.")
                .andraPatron("[^@\\s]+@[^@\\s]+\\.[^@\\s]+", "Escribe un correo válido.");
        AndraCampo<ComboBox<AndraOpcion>> andraUso = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Uso del documento", AndraOpcion.andraLista(AndraPagoServicio.ANDRA_USOS), null, andraBorradorB.andraMotivo));
        List<AndraOpcion> andraSedes = new ArrayList<>();
        andraCatalogo.andraSedes().forEach(andraPosicionX -> andraSedes.add(AndraOpcion.andraDe(andraPosicionX.getAndraCodigo(), andraPosicionX.getAndraNombreCompleto() + " · Mesa de Partes")));
        AndraCampo<ComboBox<AndraOpcion>> andraSede = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Sede de recojo", andraSedes, null, andraBorradorB.andraSede));
        List<Node> andraNodos = new ArrayList<>(List.of(andraTipo.andraNodo(), andraNombre.andraNodo(), andraDni.andraNodo(), andraCodigo.andraNodo(), andraEscuela.andraNodo(), andraCorreo.andraNodo(),
                andraUso.andraNodo(), andraSede.andraNodo()));
        AndraCampo<TextField> andraCantidad = null;
        if (andraTramite.isAndraPorCantidad()) {
            andraCantidad = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Cantidad de cursos", null, String.valueOf(andraBorradorB.andraCantidad), 2));
            andraNodos.add(andraCantidad.andraNodo());
        }
        AndraCampo<AndraFormulario.AndraArchivo> andraFoto = null;
        if (andraTramite.andraEsCarne()) {
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
            andraBorradorB.andraSol = AndraDatosSolicitante.builder().andraTipo(AndraFormulario.andraValor(andraTipo.andraControl())).andraNombre(andraNombre.andraControl().getText().trim())
                    .andraDni(andraDni.andraControl().getText().trim()).andraCodigo(andraCodigo.andraControl().getText().trim())
                    .andraEscuela(AndraFormulario.andraValor(andraEscuela.andraControl()).isBlank() ? null : AndraFormulario.andraValor(andraEscuela.andraControl()))
                    .andraCorreo(andraCorreo.andraControl().getText().trim()).build();
            andraBorradorB.andraMotivo = AndraFormulario.andraValor(andraUso.andraControl());
            andraBorradorB.andraSede = AndraFormulario.andraValor(andraSede.andraControl());
        };
        andraTipo.andraControl().valueProperty().addListener((andraObservable, andraOpcionA, andraOpcion) -> {
            if (andraOpcion == null || andraOpcionA == null || andraOpcion.andraValor().equals(andraOpcionA.andraValor())) {
                return;
            }
            andraRecolectar.run();
            andraBorradorB.andraSol.setAndraTipo(andraOpcion.andraValor());
            if ("Público en general".equals(andraOpcion.andraValor())) {
                andraBorradorB.andraSol.setAndraCodigo("");
                andraBorradorB.andraSol.setAndraEscuela(null);
            } else if (!List.of("Estudiante", "Egresado", "Docente").contains(andraOpcion.andraValor())) {
                andraBorradorB.andraSol.setAndraEscuela(null);
            } else if (andraBorradorB.andraSol.getAndraEscuela() == null || andraCatalogo.andraEscuela(andraBorradorB.andraSol.getAndraEscuela()).isEmpty()) {
                andraBorradorB.andraSol.setAndraEscuela(andraSesion.andraEstudiante().getAndraEscuela().getAndraCodigo());
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
            Map<String, String> andraErrores = andraPagos.andraValidarSolicitante(andraTramite, andraBorradorB.andraSol, andraBorradorB.andraFoto != null);
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
        andraNotificador.andraModal("Solicitar " + andraTramite.getAndraNombre().toLowerCase(), andraCuerpo, true,
                AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()), andraSiguiente);
    }

    private void andraPaso2(AndraBorrador andraBorrador) {
        AndraTramite andraTramite = andraBorrador.andraTramite;
        List<AndraElementoPago> andraElementos = AndraPagoServicio.andraElementosPara(andraTramite, andraBorrador.andraCantidad);
        BigDecimal andraTotal = AndraPagoServicio.andraTotalOrden(andraElementos);
        AndraSede andraSede = andraCatalogo.andraSede(andraBorrador.andraSede).orElseThrow();
        String andraCodigo = andraPagos.andraSiguienteCodigoOrden();
        AndraPagoServicio.AndraRecojo andraRecojo = AndraPagoServicio.andraRecojo(andraTramite, AndraFechas.andraAhora());
        HBox andraRecojo2 = new HBox(new FontIcon("fth-calendar"), new VBox(2, AndraComponentes.andraEtiqueta("Recojo estimado si pagas ahora", "strong"),
                AndraComponentes.andraParrafo(andraRecojo.andraFecha() != null ? andraRecojo.andraTexto() + " · Mesa de Partes, " + andraSede.getAndraNombreCompleto() : andraRecojo.andraTexto(), "small")));
        andraRecojo2.getStyleClass().add("wz-pick");
        VBox andraCuerpo = new VBox(14, andraPasos(2), AndraVistaFactura.andraCrear(andraCodigo, andraElementos, andraBorrador.andraSol, AndraFechas.andraHoy().plusDays(3), andraSede, null, andraCatalogo), andraRecojo2);
        Button andraVolver = AndraComponentes.andraBoton("Volver", "btn-secondary", null, andraEvento -> andraPaso1(andraBorrador));
        Button andraDespues = AndraComponentes.andraBoton("Pagar después", "btn-secondary", null, andraEvento -> {
            AndraPago andraPago = andraCrear(andraBorrador);
            if (andraPago == null) {
                return;
            }
            andraNotificador.andraCerrarModal();
            andraAlCambiar.run();
            andraNotificador.andraExito("Orden " + andraPago.getAndraCodigo() + " guardada en Andra Pay. Págala antes del " + AndraFechas.andraDmy(andraPago.getAndraVence()) + ".");
        });
        Button andraPagar = AndraComponentes.andraBoton("Pagar " + AndraMontos.andraSoles(andraTotal), "btn-primary", "fth-credit-card", andraEvento -> {
            AndraPago andraPago = andraCrear(andraBorrador);
            if (andraPago != null) {
                andraAlCambiar.run();
                andraPantalla(andraPago.getAndraCodigo(), andraAlCambiar);
            }
        });
        andraNotificador.andraModal("Orden de pago", andraCuerpo, true, andraVolver, andraDespues, andraPagar);
    }

    private AndraPago andraCrear(AndraBorrador andraBorrador) {
        try {
            return andraPagos.andraCrearOrden(andraBorrador.andraTramite, andraBorrador.andraSol, andraBorrador.andraCantidad, andraBorrador.andraMotivo, andraBorrador.andraSede, andraBorrador.andraFoto);
        } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
            andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            return null;
        }
    }

    public void andraSinConexion(AndraPago andraPago) {
        andraNotificador.andraModal("Necesitas conexión para pagar", AndraComponentes.andraParrafo("Los pagos con Yape, Plin o tarjeta requieren internet. Tu orden "
                + andraPago.getAndraCodigo() + " quedó guardada: págala cuando vuelva la conexión.", "muted"), false,
                AndraComponentes.andraBoton("Entendido", "btn-primary", null, andraEvento -> andraNotificador.andraCerrarModal()));
    }

    public void andraPantalla(String andraCodigo, Runnable andraAlTerminar) {
        andraAlCambiar(andraAlTerminar);
        AndraPago andraPago = andraPagos.andraObtener(andraCodigo);
        if (andraConexion.andraSinConexion()) {
            andraSinConexion(andraPago);
            return;
        }
        if (andraPago.andraEsPagado()) {
            andraComprobante(andraCodigo);
            return;
        }
        new AndraPantallaPago(andraPago).andraDibujar();
    }

    private final class AndraPantallaPago {
        private final AndraPago andraPago;
        private String andraMetodo;
        private String andraSub = "qr";
        private Timeline andraReloj;
        private final StackPane andraPanel = new StackPane();

        AndraPantallaPago(AndraPago andraPago) {
            this.andraPago = andraPago;
        }

        void andraDetener() {
            if (andraReloj != null) {
                andraReloj.stop();
            }
        }

        void andraDibujar() {
            andraDetener();
            BigDecimal andraTotal = andraPago.andraObtenerTotal();
            VBox andraResumen = new VBox(6, AndraComponentes.andraMayus(andraPago.getAndraTipo().andraObtenerEtiqueta(), "label-caps"),
                    AndraComponentes.andraLiteralParrafo(andraPago.getAndraConcepto(), "strong"), AndraComponentes.andraLiteral(andraPago.getAndraCodigo(), "hint"));
            VBox andraElementos = new VBox(8);
            for (AndraElementoPago andraElementoPago : andraPago.getAndraElementos()) {
                HBox andraFila = new HBox(8, AndraComponentes.andraLiteralParrafo(andraElementoPago.getAndraConcepto() + (andraElementoPago.getAndraCantidad() > 1 ? " × " + andraElementoPago.getAndraCantidad() : ""), "small"),
                        AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraElementoPago.andraObtenerImporte()), "strong", "small"));
                ((Label) andraFila.getChildren().get(2)).setMinWidth(Region.USE_PREF_SIZE);
                andraElementos.getChildren().add(andraFila);
            }
            VBox.setMargin(andraElementos, new Insets(8, 0, 0, 0));
            HBox andraTot = new HBox(AndraComponentes.andraEtiqueta("Total"), AndraComponentes.andraEspacio(), AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraTotal), "pay-total-valor"));
            andraTot.getStyleClass().add("pay-total");
            andraResumen.getChildren().addAll(andraElementos, andraTot);
            if (andraPago.getAndraVence() != null) {
                andraResumen.getChildren().add(AndraComponentes.andraEtiqueta("Vence el " + AndraFechas.andraDmy(andraPago.getAndraVence()), "hint"));
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
            if (andraPago.getAndraTramite() != null) {
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
                ImageView andraImagenQr = new ImageView(AndraQrServicio.andraImagen("ANDRA-PAY|" + andraPago.getAndraCodigo() + "|" + andraTotal.setScale(2) + "|DEMO", 336));
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
                        new HBox(4, AndraComponentes.andraEtiqueta("Monto:", "hint"), AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraTotal), "strong"),
                                AndraComponentes.andraEtiqueta("· Destino: Andra Pay", "hint")), andraVence, andraListo);
                VBox.setMargin(andraListo, new Insets(8, 0, 0, 0));
                HBox andraCaja = new HBox(18, andraMarco, andraTexto);
                andraCaja.getStyleClass().add("qr-box");
                andraContenido = andraCaja;
            } else if ("yape".equals(andraMetodo)) {
                AndraFormulario andraFormulario = new AndraFormulario();
                AndraCampo<TextField> andraCel = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Celular Yape", null, andraSesion.andraEstudiante().getAndraCelular().replaceAll("\\s", ""), 9))
                        .andraRequerido("Escribe tu celular.").andraPatron("9\\d{8}", "Celular de 9 dígitos que empiece con 9.");
                AndraCampo<TextField> andraCod = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Código de aprobación", "••••••", "", 6))
                        .andraRequerido("Escribe el código de 6 dígitos.").andraPatron("\\d{6}", "El código tiene 6 dígitos.");
                Button andraPagar = AndraComponentes.andraBloque(AndraComponentes.andraBoton("Pagar " + AndraMontos.andraSoles(andraTotal) + " con Yape", "btn-primary", null, andraEvento -> {
                    if (andraFormulario.andraValidar() && AndraPagoServicio.andraValidarYape(andraCel.andraControl().getText(), andraCod.andraControl().getText()).isEmpty()) {
                        andraProcesar("yape");
                    }
                }));
                andraPagar.setDefaultButton(true);
                andraContenido = new VBox(12, AndraComponentes.andraParrafo("Abre Yape, entra al menú y toca Código de aprobación. Tienes unos minutos para usarlo.", "hint"),
                        AndraComponentes.andraParrafo("Abre Yape › Código de aprobación", "small", "azul"),
                        AndraFormulario.andraRejilla(2, andraCel.andraNodo(), andraCod.andraNodo()), andraPagar);
            } else {
                AndraFormulario andraFormulario = new AndraFormulario();
                AndraCampo<TextField> andraCel = andraFormulario.andraAgregar(AndraFormulario.andraNumero("Celular Plin", null, andraSesion.andraEstudiante().getAndraCelular().replaceAll("\\s", ""), 9))
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
                String andraFormato = AndraTarjetaJemm.andraFormatear(andraTextoB);
                if (!andraFormato.equals(andraTextoB)) {
                    javafx.application.Platform.runLater(() -> {
                        andraNumero.andraControl().setText(andraFormato);
                        andraNumero.andraControl().positionCaret(andraFormato.length());
                    });
                }
                AndraMetodoPago andraMetodoPago = AndraTarjetaJemm.andraMarca(andraTextoB);
                andraMarca.setText(andraMetodoPago == null ? "—" : andraMetodoPago.andraObtenerEtiqueta());
            });
            HBox andraFilaNumero = new HBox(8, andraNumero.andraNodo(), andraMarca);
            HBox.setHgrow(andraNumero.andraNodo(), Priority.ALWAYS);
            andraFilaNumero.setAlignment(Pos.CENTER_LEFT);
            AndraCampo<TextField> andraTitular = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Nombre del titular", null, andraSesion.andraEstudiante().getAndraNombre().toUpperCase()))
                    .andraRequerido("Escribe el nombre como aparece en la tarjeta.");
            AndraCampo<TextField> andraVence = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Vencimiento", "MM/AA", "")).andraRequerido("Escribe el vencimiento.");
            andraVence.andraControl().textProperty().addListener((andraObservable, andraTextoA, andraTextoB) -> {
                String andraFormato = AndraTarjetaJemm.andraFormatearVencimiento(andraTextoB);
                if (!andraFormato.equals(andraTextoB) && !(andraTextoA != null && andraTextoA.length() > andraTextoB.length())) {
                    javafx.application.Platform.runLater(() -> {
                        andraVence.andraControl().setText(andraFormato);
                        andraVence.andraControl().positionCaret(andraFormato.length());
                    });
                }
            });
            AndraCampo<TextField> andraCvv = andraFormulario.andraAgregar(AndraFormulario.andraNumero("CVV", "123", "", 4)).andraRequerido("Escribe el CVV.");
            AndraCampo<TextField> andraCorreo = andraFormulario.andraAgregar(AndraFormulario.andraTexto("Correo para el comprobante", null, andraSesion.andraEstudiante().getAndraCorreo()))
                    .andraRequerido("Escribe un correo.");
            Button andraPagar = AndraComponentes.andraBloque(AndraComponentes.andraBoton("Pagar " + AndraMontos.andraSoles(andraTotal), "btn-primary", "fth-lock", null));
            andraPagar.setDefaultButton(true);
            andraPagar.setOnAction(andraEvento -> {
                if (!andraFormulario.andraValidar()) {
                    return;
                }
                Map<String, String> andraErrores = AndraPagoServicio.andraValidarTarjeta(andraNumero.andraControl().getText(), andraTitular.andraControl().getText(),
                        andraVence.andraControl().getText(), andraCvv.andraControl().getText(), andraCorreo.andraControl().getText(), YearMonth.now(AndraFechas.andraReloj()));
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
                AndraMetodoPago andraMetodoPago = AndraTarjetaJemm.andraMarca(andraNumero.andraControl().getText());
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
                    andraPagos.andraCompletar(andraPago.getAndraCodigo(), AndraMetodoPago.andraDesdeClave(andraClave));
                    andraAlCambiar.run();
                    andraExito(andraPago.getAndraCodigo());
                } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
                    andraNotificador.andraCerrarModal();
                    if (andraConexion.andraSinConexion()) {
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
        AndraPago andraPago = andraPagos.andraObtener(andraCodigo);
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
        VBox andraPortada = new VBox(6, andraVerificacion, AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraPago.andraObtenerTotal()), "ok-monto"), AndraComponentes.andraLiteralParrafo(andraPago.getAndraConcepto()));
        andraPortada.getStyleClass().add("ok-hero");
        Object andraFilaFinal;
        String andraEtiquetaFinal;
        if (andraPago.getAndraTipo() == AndraTipoPago.MATRICULA) {
            andraEtiquetaFinal = "Matrícula";
            andraFilaFinal = AndraComponentes.andraPildora("Confirmada", "success");
        } else {
            andraEtiquetaFinal = "Recojo";
            VBox andraCajaVertical = new VBox(2, AndraComponentes.andraParrafo(andraPago.getAndraRecojoTexto(), "dd"));
            if (andraPago.getAndraRecojoFecha() != null) {
                andraCajaVertical.getChildren().add(AndraComponentes.andraParrafo("Mesa de Partes · " + andraPago.getAndraSede().getAndraNombreCompleto() + ". Lleva tu DNI.", "hint"));
            }
            andraFilaFinal = andraCajaVertical;
        }
        javafx.scene.layout.GridPane andraParesClaveValor = AndraComponentes.andraKv("Método", andraPago.getAndraMetodo().andraObtenerEtiqueta(), "N.° de operación", AndraComponentes.andraLiteral(andraPago.getAndraOperacion(), "dd"),
                "Comprobante", AndraComponentes.andraLiteral(andraPago.getAndraBoleta(), "dd"), "Fecha y hora", AndraComponentes.andraLiteral(AndraFechas.andraDmyHm(andraPago.getAndraPagado()), "dd"),
                andraEtiquetaFinal, andraFilaFinal);
        andraParesClaveValor.setMaxWidth(520);
        VBox andraCuerpo = new VBox(18);
        if (andraPago.getAndraTramite() != null) {
            andraCuerpo.getChildren().add(andraPasos(4));
        }
        andraCuerpo.getChildren().addAll(andraPortada, andraParesClaveValor);
        andraCuerpo.setAlignment(Pos.TOP_CENTER);
        List<Node> andraPie = new ArrayList<>();
        andraPie.add(AndraComponentes.andraBoton("Ver comprobante", "btn-secondary", "fth-file", andraEvento -> andraComprobante(andraCodigo)));
        if (andraPago.getAndraTramite() != null && andraPago.getAndraTramite().andraGeneraDocumento()) {
            andraPie.add(AndraComponentes.andraBoton("Ver documento", "btn-secondary", "fth-file-text", andraEvento -> andraDocumento(andraCodigo)));
        }
        andraPie.add(AndraComponentes.andraBoton("Listo", "btn-primary", null, andraEvento -> {
            andraNotificador.andraCerrarModal();
            andraAlCambiar.run();
        }));
        andraNotificador.andraModal("Pago confirmado", andraCuerpo, true, andraPie.toArray(new Node[0]));
    }

    public void andraComprobante(String andraCodigo) {
        AndraPago andraPago = andraPagos.andraObtener(andraCodigo);
        StackPane andraHoja = AndraVistaFactura.andraCrear(andraPago.getAndraCodigo(), andraPago.getAndraElementos(), andraGenerador.andraSolicitante(andraPago), andraPago.getAndraVence(), andraPago.getAndraSede(), andraPago, andraCatalogo);
        andraNotificador.andraModal("Comprobante de pago", andraHoja, true,
                AndraComponentes.andraBoton("Copiar N.° de operación", "btn-secondary", null, andraEvento -> {
                    AndraPortapapeles.andraCopiar(andraPago.getAndraOperacion());
                    andraNotificador.andraExito("Número copiado.");
                }),
                AndraComponentes.andraBoton("Descargar", "btn-primary", "fth-download", andraEvento -> andraDescargas.andraGuardarPdf("Comprobante " + andraPago.getAndraBoleta(),
                        () -> andraGenerador.andraPdfComprobante(andraPago))));
    }

    public void andraDocumento(String andraCodigo) {
        AndraPago andraPago = andraPagos.andraObtener(andraCodigo);
        VBox andraCuerpo = new VBox(12, AndraComponentes.andraParrafo("Generado automáticamente con los datos del solicitante. Documento de demostración sin valor oficial.", "hint"),
                AndraVistaDocumento.andraCrear(andraGenerador.andraConstruir(andraPago)));
        andraNotificador.andraModal("Documento generado", andraCuerpo, true,
                AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Descargar PDF", "btn-primary", "fth-download", andraEvento -> andraDescargas.andraGuardarPdf(andraPago.getAndraConcepto() + " " + (andraPago.getAndraBoleta() == null ? "" : andraPago.getAndraBoleta()),
                        () -> andraGenerador.andraPdfDocumento(andraPago))));
    }

    public void andraDetalle(String andraCodigo, Runnable andraAlTerminar) {
        andraAlCambiar(andraAlTerminar);
        AndraPago andraPago = andraPagos.andraObtener(andraCodigo);
        if (!andraPago.andraEsPendiente()) {
            andraComprobante(andraCodigo);
            return;
        }
        FlowPane andraMetodos = new FlowPane(6, 6, AndraComponentes.andraEtiqueta("Puedes pagar con", "hint"), AndraComponentes.andraLiteral("Yape", "pm-chip", "y"),
                AndraComponentes.andraLiteral("Plin", "pm-chip", "p"), AndraComponentes.andraLiteral("Visa", "pm-chip"), AndraComponentes.andraLiteral("Mastercard", "pm-chip"));
        andraMetodos.setAlignment(Pos.CENTER);
        VBox andraCuerpo = new VBox(14, AndraVistaFactura.andraCrear(andraPago.getAndraCodigo(), andraPago.getAndraElementos(), andraGenerador.andraSolicitante(andraPago), andraPago.getAndraVence(), andraPago.getAndraSede(), null, andraCatalogo), andraMetodos);
        andraNotificador.andraModal("Detalle del derecho de pago", andraCuerpo, true,
                AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Pagar " + AndraMontos.andraSoles(andraPago.andraObtenerTotal()), "btn-primary", "fth-credit-card", andraEvento -> andraPantalla(andraCodigo, andraAlCambiar)));
    }

    public void andraNuevoPago(Runnable andraAlTerminar) {
        andraAlCambiar(andraAlTerminar);
        VBox andraLista = new VBox(8);
        for (AndraTramite andraTramite : andraCatalogo.andraTramites()) {
            VBox andraTextos = new VBox(2, AndraComponentes.andraParrafo(andraTramite.getAndraNombre(), "strong"), AndraComponentes.andraEtiqueta(andraTramite.getAndraCategoria() + " · " + andraTramite.andraObtenerTextoEntrega(), "hint"));
            HBox.setHgrow(andraTextos, Priority.ALWAYS);
            HBox andraContenido = new HBox(10, andraTextos, AndraComponentes.andraLiteral(AndraMontos.andraSoles(andraTramite.getAndraMonto()), "strong"));
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
