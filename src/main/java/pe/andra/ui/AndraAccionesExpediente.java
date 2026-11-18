package pe.andra.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.util.DixonFechas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AndraAccionesExpediente {

    private final AndraNotificador andraNotificador;
    private final DixonExpedienteServicio andraExpedientes;
    private final DixonSeguimientoServicio andraSeguimiento;
    private final DixonSesionServicio andraSesion;
    private final DixonCatalogoServicio andraCatalogo;
    private final DixonContextoServicio andraContexto;
    private final AndraAccionesPago andraAccionesPago;
    private final AndraDescargas andraDescargas;
    private final DixonDocumentoGeneradoServicio andraGenerador;

    public void andraSeguimiento(String andraCodigo) {
        DixonExpediente andraExpediente = andraExpedientes.dixonObtener(andraCodigo);
        DixonSeguimientoServicio.DixonSeguimientoCjsv andraSeguimientoCjsv = DixonSeguimientoServicio.dixonCalcular(andraExpediente);
        VBox andraPortada = new VBox(4, AndraComponentes.andraLiteral(andraExpediente.getDixonCodigo(), "label-caps"), AndraComponentes.andraParrafo(andraSeguimientoCjsv.dixonEstimado(), "trk-hero-titulo"),
                AndraComponentes.andraLiteralParrafo(andraExpediente.getDixonTitulo(), "muted", "small"));
        andraPortada.getStyleClass().add("trk-hero");
        if (andraSeguimientoCjsv.dixonObservado()) {
            andraPortada.getStyleClass().add("trk-obs");
        }
        VBox andraTiempo = AndraRastreadorCjsv.andraLineaTiempo(andraSeguimientoCjsv);
        DixonPersona andraEstudianteActual = andraExpediente.getDixonPersona();
        DixonSeguimientoServicio.DixonPreferencias andraPref = andraSeguimiento.dixonPreferencias();
        VBox andraAvisos = new VBox(AndraComponentes.andraMayus("Avísame cuando cambie", "label-caps"),
                andraInterruptor("fth-message-circle", "WhatsApp", andraEstudianteActual.getDixonCelular() == null ? null : "+51 " + andraEstudianteActual.getDixonCelular(), andraPref.dixonWhatsapp(), "wa"),
                andraInterruptor("fth-mail", "Correo", andraEstudianteActual.getDixonCorreo(), andraPref.dixonCorreo(), "mail"),
                andraInterruptor("fth-bell", "Notificación en Andra", null, andraPref.dixonAplicacion(), "app"));
        VBox andraMensajes = new VBox(8);
        List<DixonMensajeEnviado> andraLista = andraSeguimiento.dixonMensajes(andraExpediente);
        if (andraLista.isEmpty()) {
            andraMensajes.getChildren().add(AndraComponentes.andraParrafo("Aún no hay mensajes. Te escribiremos cuando tu trámite avance.", "hint"));
        }
        for (DixonMensajeEnviado andraMensajeEnviado : andraLista) {
            VBox andraBurbuja = new VBox(AndraComponentes.andraEtiqueta(andraMensajeEnviado.getDixonCanal().dixonObtenerEtiqueta() + " · " + DixonFechas.dixonHm(andraMensajeEnviado.getDixonEnviado()), "wa-c"),
                    AndraComponentes.andraLiteralParrafo(andraMensajeEnviado.getDixonTexto(), "small"));
            andraBurbuja.getStyleClass().add("wa-msg");
            if (andraMensajeEnviado.getDixonCanal() == DixonCanalAviso.CORREO) {
                andraBurbuja.getStyleClass().add("mail");
            }
            andraMensajes.getChildren().add(andraBurbuja);
        }
        Label andraTitMensajes = AndraComponentes.andraMayus("Mensajes enviados", "label-caps");
        VBox.setMargin(andraTitMensajes, new Insets(16, 0, 8, 0));
        VBox andraDerecha = new VBox(andraAvisos, andraTitMensajes, andraMensajes, AndraComponentes.andraParrafo("Envío simulado en esta demostración.", "hint"));
        GridPane andraRejilla = new GridPane();
        andraRejilla.setHgap(24);
        ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
        andraRestriccionColumna1.setPercentWidth(50);
        ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
        andraRestriccionColumna2.setPercentWidth(50);
        andraRejilla.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2);
        andraRejilla.add(andraTiempo, 0, 0);
        andraRejilla.add(andraDerecha, 1, 0);
        GridPane.setValignment(andraTiempo, javafx.geometry.VPos.TOP);
        GridPane.setValignment(andraDerecha, javafx.geometry.VPos.TOP);
        VBox andraCuerpo = new VBox(18, andraPortada, andraRejilla);
        andraNotificador.andraModal("Seguimiento en vivo", andraCuerpo, true, AndraComponentes.andraBoton("Entendido", "btn-primary", null, andraEvento -> andraNotificador.andraCerrarModal()));
    }

    private HBox andraInterruptor(String andraIcono, String andraNombre, String andraDetalle, boolean andraActivo, String andraCanal) {
        CheckBox andraCasilla = new CheckBox();
        andraCasilla.setSelected(andraActivo);
        AndraComponentes.andraAccesible(andraCasilla, andraNombre);
        andraCasilla.selectedProperty().addListener((andraObservable, andraBanderaA, andraBanderaB) -> {
            andraSeguimiento.dixonCambiarPreferencia(andraCanal, andraBanderaB);
            andraNotificador.andraInformacion(andraBanderaB ? "Aviso activado." : "Aviso desactivado.");
        });
        HBox andraFila = new HBox(10, andraCasilla, AndraComponentes.andraIcono(andraIcono), AndraComponentes.andraEtiqueta(andraNombre));
        if (andraDetalle != null) {
            andraFila.getChildren().add(AndraComponentes.andraLiteral(andraDetalle, "hint"));
        }
        andraFila.getStyleClass().add("sw");
        andraFila.setOnMouseClicked(andraEvento -> {
            if (andraEvento.getTarget() != andraCasilla) {
                andraCasilla.setSelected(!andraCasilla.isSelected());
            }
        });
        return andraFila;
    }

    public void andraCargo(String andraCodigo) {
        DixonExpediente andraExpediente = andraExpedientes.dixonObtener(andraCodigo);
        List<Object> andraPares = new ArrayList<>(List.of("Expediente", AndraComponentes.andraLiteral(andraExpediente.getDixonCodigo(), "dd"), "Trámite", AndraComponentes.andraParrafo(andraExpediente.getDixonTitulo(), "dd")));
        andraExpediente.getDixonDatos().forEach(andraDatoExpediente -> {
            andraPares.add(andraDatoExpediente.getDixonEtiqueta());
            andraPares.add(AndraComponentes.andraParrafo(andraDatoExpediente.getDixonValor(), "dd"));
        });
        andraPares.add("Titular");
        andraPares.add(AndraComponentes.andraLiteral(andraExpediente.getDixonPersona().getDixonNombre() + " (" + andraExpediente.getDixonPersona().getDixonCodigo() + ")", "dd"));
        String andraTitulo = andraExpediente.getDixonClase() == DixonClaseExpediente.EQ ? "Cargo digital" : "Cargo de entrega";
        andraNotificador.andraModal(andraTitulo, AndraComponentes.andraKv(andraPares.toArray()), false,
                AndraComponentes.andraBoton("Cerrar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Descargar", "btn-primary", "fth-download", andraEvento -> andraDescargas.andraGuardarPdf("Cargo " + andraExpediente.getDixonCodigo(),
                        () -> andraGenerador.dixonPdfSimple(andraTitulo + " " + andraExpediente.getDixonCodigo(), andraExpediente.getDixonTitulo()))));
    }

    public void andraEstado(String andraCodigo) {
        DixonExpediente andraExpediente = andraExpedientes.dixonObtener(andraCodigo);
        int andraPaso = Math.max(1, Math.min(4, andraExpediente.getDixonPaso()));
        VBox andraCuerpo = new VBox(12, AndraComponentes.andraPildora(andraExpediente.getDixonEstado(), andraExpediente.getDixonTono()), AndraComponentes.andraKv("Expediente", AndraComponentes.andraLiteral(andraExpediente.getDixonCodigo(), "dd"),
                "Paso actual", andraPaso + ". " + DixonExpedienteServicio.DIXON_PASOS.get(andraPaso - 1)[1], "Plazo de respuesta", "48 horas útiles",
                "Última actualización", "Hoy"));
        andraNotificador.andraModal("Estado del expediente", andraCuerpo, false, AndraComponentes.andraBoton("Cerrar", "btn-primary", null, andraEvento -> andraNotificador.andraCerrarModal()));
    }

    public void andraDescargar(String andraCodigo) {
        DixonExpediente andraExpediente = andraExpedientes.dixonObtener(andraCodigo);
        andraDescargas.andraGuardarPdf(andraExpediente.getDixonTitulo(), () -> andraGenerador.dixonPdfSimple(andraExpediente.getDixonTitulo(), andraExpediente.getDixonCodigo() + " · " + andraExpediente.getDixonEstado()));
    }

    public void andraSubsanar(String andraCodigo, Runnable andraAlCambiar) {
        DixonExpediente andraExpediente = andraExpedientes.dixonObtener(andraCodigo);
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<AndraFormulario.AndraArchivo> andraArchivo = andraFormulario.andraAgregar(AndraFormulario.andraArchivo("Documento de subsanación (PDF)", "*.pdf", "Solo PDF. Máximo 5 MB.", andraNotificador, null))
                .andraRequerido("Adjunta el documento en PDF.");
        AndraCampo<TextArea> andraComentario = andraFormulario.andraAgregar(AndraFormulario.andraArea("Comentario para Secretaría (opcional)", "Ej. Adjunto certificado con visado del Servicio Médico."));
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Expediente " + andraExpediente.getDixonCodigo() + ". Adjunta la constancia visada por el Servicio Médico.", "muted", "small"),
                andraArchivo.andraNodo(), andraComentario.andraNodo());
        Button andraEnviar = AndraComponentes.andraBoton("Enviar subsanación", "btn-primary", null, andraEvento -> {
            if (!andraFormulario.andraValidar()) {
                return;
            }
            try {
                andraExpedientes.dixonSubsanar(andraCodigo, andraArchivo.andraTexto());
                andraNotificador.andraCerrarModal();
                andraAlCambiar.run();
                andraNotificador.andraExito("Subsanación enviada. El plazo del trámite continúa.");
            } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            }
        });
        andraNotificador.andraModal("Subsanar documento", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()), andraEnviar);
    }

    public void andraDesistir(String andraCodigo, Runnable andraAlCambiar) {
        andraNotificador.andraConfirmar("¿Desistir de este trámite?", "El expediente " + andraCodigo + " se anulará. Si pagaste una tasa, Tesorería la reintegrará en 5 días hábiles.",
                "Desistir del trámite", true, () -> {
                    andraExpedientes.dixonDesistir(andraCodigo);
                    andraAlCambiar.run();
                    andraNotificador.andraExito("Trámite anulado.");
                });
    }

    public void andraCancelar(String andraCodigo, Runnable andraAlCambiar) {
        DixonExpediente andraExpediente = andraExpedientes.dixonObtener(andraCodigo);
        andraNotificador.andraConfirmar(andraExpediente.getDixonClase() == DixonClaseExpediente.LAB ? "¿Cancelar la reserva?" : "¿Cancelar el préstamo?",
                "El expediente " + andraCodigo + " quedará cancelado. Hazlo con al menos 30 minutos de anticipación para evitar penalizaciones.",
                "Sí, cancelar", true, () -> {
                    andraExpedientes.dixonCancelar(andraCodigo);
                    andraAlCambiar.run();
                    andraNotificador.andraExito("Cancelado correctamente.");
                });
    }

    public void andraConsultar(String andraCodigo) {
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<TextArea> andraMensaje = andraFormulario.andraAgregar(AndraFormulario.andraArea("Tu consulta", null)).andraRequerido("Escribe tu consulta.").andraMinimo(10);
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Expediente " + andraCodigo + ". Responderemos en un plazo de 48 horas útiles.", "muted", "small"), andraMensaje.andraNodo());
        andraNotificador.andraModal("Consultar a Secretaría", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Enviar consulta", "btn-primary", null, andraEvento -> {
                    if (andraFormulario.andraValidar()) {
                        andraNotificador.andraCerrarModal();
                        andraNotificador.andraExito("Consulta enviada a Secretaría.");
                    }
                }));
    }

    public void andraPase(String andraCodigo) {
        DixonExpediente andraExpediente = andraExpedientes.dixonObtener(andraCodigo);
        ImageView andraImagenQr = new ImageView(DixonQrServicio.dixonImagen("ANDRA-LAB|" + andraExpediente.getDixonCodigo() + "|DEMO", 320));
        andraImagenQr.setFitWidth(160);
        andraImagenQr.setFitHeight(160);
        StackPane andraMarco = new StackPane(andraImagenQr);
        andraMarco.getStyleClass().add("qr-img");
        andraMarco.setMaxWidth(Region.USE_PREF_SIZE);
        String andraClave = andraExpediente.getDixonEtiquetas().stream().filter(andraPosicionX -> andraPosicionX.contains("LAB-")).map(andraPosicionX -> andraPosicionX.substring(andraPosicionX.indexOf("LAB-"))).findFirst().orElse("LAB-992-K");
        String andraLugar = andraExpediente.getDixonDatos().size() > 1 ? andraExpediente.getDixonDatos().get(1).getDixonValor() : "";
        String andraFecha = andraExpediente.getDixonDatos().isEmpty() ? "" : andraExpediente.getDixonDatos().get(0).getDixonValor();
        VBox andraCuerpo = new VBox(8, andraMarco, AndraComponentes.andraMayus("Código de llave / acceso", "label-caps"), AndraComponentes.andraLiteral(andraClave, "dato-grande"),
                AndraComponentes.andraLiteralParrafo(andraLugar + " · " + andraFecha, "muted", "small"), AndraComponentes.andraParrafo("Código ilustrativo de demostración.", "hint"));
        andraCuerpo.setAlignment(Pos.CENTER);
        andraNotificador.andraModal("Pase de acceso", andraCuerpo, false, AndraComponentes.andraBoton("Listo", "btn-primary", null, andraEvento -> andraNotificador.andraCerrarModal()));
    }

    public void andraReprogramar(String andraCodigo, Runnable andraAlCambiar) {
        DixonExpediente andraExpediente = andraExpedientes.dixonObtener(andraCodigo);
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<DatePicker> andraFecha = andraFormulario.andraAgregar(AndraFormulario.andraFecha("Nueva fecha", DixonFechas.dixonHoy(), DixonFechas.dixonHoy(), null)).andraRequerido("Elige una fecha.");
        AndraCampo<ComboBox<AndraOpcion>> andraHora = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Nuevo horario", AndraOpcion.andraLista(DixonExpedienteServicio.dixonHorariosReprogramacion(andraExpediente)),
                "Seleccionar", null)).andraRequerido("Elige un horario.");
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Expediente " + andraExpediente.getDixonCodigo() + ". El cambio queda sujeto a disponibilidad.", "muted", "small"),
                AndraFormulario.andraRejilla(2, andraFecha.andraNodo(), andraHora.andraNodo()));
        andraNotificador.andraModal(andraExpediente.getDixonClase() == DixonClaseExpediente.LAB ? "Modificar horario" : "Modificar retiro", andraCuerpo, false,
                AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Guardar cambio", "btn-primary", null, andraEvento -> {
                    if (!andraFormulario.andraValidar()) {
                        return;
                    }
                    try {
                        andraExpedientes.dixonReprogramar(andraCodigo, andraFecha.andraControl().getValue(), AndraFormulario.andraValor(andraHora.andraControl()));
                        andraNotificador.andraCerrarModal();
                        andraAlCambiar.run();
                        andraNotificador.andraExito("Horario actualizado.");
                    } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                        andraNotificador.andraError(andraNegocioExcepcion.getMessage());
                    }
                }));
    }

    public void andraNueva(Runnable andraAlCambiar) {
        AndraFormulario andraFormulario = new AndraFormulario();
        List<AndraOpcion> andraTipos = new ArrayList<>();
        for (DixonExpedienteServicio.DixonGrupoOpciones andraGrupoOpciones : DixonExpedienteServicio.DIXON_TIPOS_SOLICITUD) {
            andraTipos.add(AndraOpcion.andraGrupo(andraGrupoOpciones.dixonGrupo()));
            andraGrupoOpciones.dixonOpciones().forEach(andraTexto -> andraTipos.add(AndraOpcion.andraDe(andraTexto, andraTexto)));
        }
        AndraCampo<ComboBox<AndraOpcion>> andraTipo = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Tipo de trámite", andraTipos, "Seleccionar", null)).andraRequerido("Elige el tipo de trámite.");
        List<AndraOpcion> andraUnidades = new ArrayList<>();
        andraUnidades.add(AndraOpcion.andraGrupo("Escuelas profesionales"));
        andraCatalogo.dixonEscuelas().forEach(andraEscuelaProfesional -> andraUnidades.add(AndraOpcion.andraDe("EP " + andraEscuelaProfesional.getDixonNombre(), andraEscuelaProfesional.getDixonNombre())));
        andraUnidades.add(AndraOpcion.andraGrupo("Dependencias administrativas"));
        andraCatalogo.dixonDependencias().forEach(andraTexto2 -> andraUnidades.add(AndraOpcion.andraDe(andraTexto2, andraTexto2)));
        DixonEscuelaProfesional andraEscuela = andraContexto.dixonEscuela();
        String andraUnidadInicial = "EP " + (andraEscuela != null ? andraEscuela.getDixonNombre() : andraSesion.dixonEstudiante().getDixonEscuela().getDixonNombre());
        AndraCampo<ComboBox<AndraOpcion>> andraUnidad = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Unidad académica", andraUnidades, "Seleccionar", andraUnidadInicial))
                .andraRequerido("Elige la unidad académica.");
        List<AndraOpcion> andraSedes = new ArrayList<>();
        andraCatalogo.dixonSedes().forEach(andraSede -> andraSedes.add(AndraOpcion.andraDe(andraSede.getDixonCodigo(), andraSede.getDixonNombreCompleto())));
        AndraCampo<ComboBox<AndraOpcion>> andraSede2 = andraFormulario.andraAgregar(AndraFormulario.andraSelector("Sede de atención", andraSedes, null,
                andraContexto.dixonTodasLasSedes() ? andraSesion.dixonEstudiante().getDixonSede().getDixonCodigo() : andraContexto.dixonCodigoSede()));
        AndraCampo<TextArea> andraDetalle = andraFormulario.andraAgregar(AndraFormulario.andraArea("Detalle de la solicitud", "Ej. Necesito la constancia para presentarla a mi centro de prácticas."))
                .andraRequerido("Describe tu solicitud.").andraMinimo(15);
        AndraCampo<AndraFormulario.AndraArchivo> andraAdjunto = andraFormulario.andraAgregar(AndraFormulario.andraArchivo("Documento de sustento (opcional)", "*.pdf *.jpg *.png", null, andraNotificador, null));
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo("Tu expediente ingresará a Mesa de Partes Virtual. Si el trámite tiene costo (constancias, certificados, carné), generaremos la orden de pago con tus datos para pagarla en Andra Pay.", "muted", "small"),
                andraTipo.andraNodo(), andraUnidad.andraNodo(), andraSede2.andraNodo(), andraDetalle.andraNodo(), andraAdjunto.andraNodo());
        Button andraRegistrar = AndraComponentes.andraBoton("Registrar solicitud", "btn-primary", null, andraEvento -> {
            String andraTexto3 = AndraFormulario.andraValor(andraTipo.andraControl());
            Optional<DixonTramite> andraConPago = andraExpedientes.dixonTramitePara(andraTexto3);
            if (andraConPago.isPresent()) {
                andraAccionesPago.andraIniciar(andraConPago.get(), null, andraAlCambiar);
                return;
            }
            if (!andraFormulario.andraValidar()) {
                return;
            }
            try {
                DixonExpediente andraNuevo = andraExpedientes.dixonRegistrar(andraTexto3, AndraFormulario.andraValor(andraUnidad.andraControl()), AndraFormulario.andraValor(andraSede2.andraControl()),
                        andraDetalle.andraControl().getText(), andraAdjunto.andraTexto());
                andraNotificador.andraCerrarModal();
                andraAlCambiar.run();
                andraNotificador.andraExito("Solicitud " + andraNuevo.getDixonCodigo() + " registrada.");
            } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                andraNotificador.andraError(andraNegocioExcepcion.getMessage());
            }
        });
        andraNotificador.andraModal("Nueva solicitud", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()), andraRegistrar);
    }

    public void andraEjecutar(String andraAccion, DixonExpediente andraExpediente, Runnable andraAlCambiar, Runnable andraAlternarObservacion) {
        switch (andraAccion) {
            case "route-sol", "track-sol" -> andraSeguimiento(andraExpediente.getDixonCodigo());
            case "download-sol" -> andraDescargar(andraExpediente.getDixonCodigo());
            case "cargo-sol" -> andraCargo(andraExpediente.getDixonCodigo());
            case "state-sol" -> andraEstado(andraExpediente.getDixonCodigo());
            case "desist-sol" -> andraDesistir(andraExpediente.getDixonCodigo(), andraAlCambiar);
            case "fix-sol" -> andraSubsanar(andraExpediente.getDixonCodigo(), andraAlCambiar);
            case "obs-sol" -> andraAlternarObservacion.run();
            case "ask-sol" -> andraConsultar(andraExpediente.getDixonCodigo());
            case "pass-sol" -> andraPase(andraExpediente.getDixonCodigo());
            case "resched-sol" -> andraReprogramar(andraExpediente.getDixonCodigo(), andraAlCambiar);
            case "cancel-sol" -> andraCancelar(andraExpediente.getDixonCodigo(), andraAlCambiar);
            case "pay-sol" -> andraAccionesPago.andraPantalla(andraExpediente.getDixonPago().getDixonCodigo(), andraAlCambiar);
            case "rcpt-sol" -> andraAccionesPago.andraComprobante(andraExpediente.getDixonPago().getDixonCodigo());
            case "gen-sol" -> andraAccionesPago.andraDocumento(andraExpediente.getDixonPago().getDixonCodigo());
            default -> andraNotificador.andraInformacion("Acción no disponible en la demostración.");
        }
    }

    public static Node andraIconoExpediente(String andraIcono) {
        return new FontIcon(switch (andraIcono) {
            case "alert" -> "fth-alert-triangle";
            case "lab" -> "fth-droplet";
            case "box" -> "fth-box";
            case "idcard" -> "fth-credit-card";
            default -> "fth-file-text";
        });
    }
}
