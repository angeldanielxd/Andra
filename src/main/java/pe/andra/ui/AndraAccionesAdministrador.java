package pe.andra.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonMontos;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraAccionesAdministrador {

    private final AndraNotificador andraNotificador;
    private final DixonAdministradorServicio andraAdministrador;
    private final DixonCatalogoServicio andraCatalogo;
    private final AndraAccionesPago andraAccionesPago;
    private final DixonDocumentoGeneradoServicio andraGenerador;

    public DixonDatosSolicitante andraSolicitante(DixonExpediente andraExpediente) {
        if (andraExpediente.getDixonSolicitante() != null && andraExpediente.getDixonSolicitante().getDixonNombre() != null) {
            return andraExpediente.getDixonSolicitante();
        }
        if (andraExpediente.getDixonPago() != null && andraExpediente.getDixonPago().getDixonSolicitante() != null && andraExpediente.getDixonPago().getDixonSolicitante().getDixonNombre() != null) {
            return andraExpediente.getDixonPago().getDixonSolicitante();
        }
        DixonPersona andraPersona = andraExpediente.getDixonPersona();
        return DixonDatosSolicitante.builder().dixonTipo(andraPersona.getDixonTipo()).dixonNombre(andraPersona.getDixonNombre()).dixonDni(andraPersona.getDixonDni()).dixonCodigo(andraPersona.getDixonCodigo())
                .dixonEscuela(andraPersona.getDixonEscuela() == null ? null : andraPersona.getDixonEscuela().getDixonCodigo()).dixonCorreo(andraPersona.getDixonCorreo()).build();
    }

    public void andraRevisar(String andraCodigo, Runnable andraAlCambiar) {
        DixonExpediente andraExpediente = andraAdministrador.dixonExpediente(andraCodigo);
        DixonPago andraPago = andraExpediente.getDixonPago();
        DixonDatosSolicitante andraSol = andraSolicitante(andraExpediente);
        FlowPane andraFichas = AndraComponentes.andraFichas(AndraComponentes.andraPildora(andraExpediente.getDixonEstado(), andraExpediente.getDixonTono()), AndraComponentes.andraLiteral(andraExpediente.getDixonCodigo(), "tag"),
                AndraComponentes.andraDistintivo("Sede " + andraExpediente.getDixonSede().getDixonNombre()));
        List<Object> andraDatos = new ArrayList<>(List.of("Nombre", AndraComponentes.andraLiteralParrafo(andraSol.getDixonNombre(), "dd"), "Tipo", andraSol.getDixonTipo(),
                "DNI", AndraComponentes.andraLiteral(andraSol.getDixonDni() == null ? "—" : andraSol.getDixonDni(), "dd"), "Código", AndraComponentes.andraLiteral(andraSol.getDixonCodigo() == null ? "—" : andraSol.getDixonCodigo(), "dd")));
        if (andraSol.getDixonEscuela() != null && andraCatalogo.dixonEscuela(andraSol.getDixonEscuela()).isPresent()) {
            andraDatos.add("Escuela");
            andraDatos.add(AndraComponentes.andraLiteralParrafo("EP " + andraCatalogo.dixonNombreEscuela(andraSol.getDixonEscuela()), "dd"));
        }
        if (andraSol.getDixonCorreo() != null) {
            andraDatos.add("Correo");
            andraDatos.add(AndraComponentes.andraLiteral(andraSol.getDixonCorreo(), "dd"));
        }
        VBox andraIzquierda = new VBox(8, AndraComponentes.andraMayus("Solicitante", "label-caps"), AndraComponentes.andraKv(andraDatos.toArray()));
        VBox andraDerecha = new VBox(8, AndraComponentes.andraMayus("Pago", "label-caps"));
        if (andraPago != null) {
            List<Object> andraPagoDatos = new ArrayList<>(List.of("Orden", AndraComponentes.andraLiteral(andraPago.getDixonCodigo(), "dd"), "Monto", DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()),
                    "Estado", andraPago.dixonEsPagado() ? AndraComponentes.andraPildora("Pagado · " + andraPago.getDixonMetodo().dixonObtenerEtiqueta(), "success") : AndraComponentes.andraPildora("Pendiente", "warning")));
            if (andraPago.getDixonBoleta() != null) {
                andraPagoDatos.add("Comprobante");
                andraPagoDatos.add(AndraComponentes.andraLiteral(andraPago.getDixonBoleta(), "dd"));
            }
            andraDerecha.getChildren().add(AndraComponentes.andraKv(andraPagoDatos.toArray()));
        } else {
            andraDerecha.getChildren().add(AndraComponentes.andraParrafo("Este trámite no tiene costo.", "muted", "small"));
        }
        if (andraExpediente.getDixonObservacion() != null && andraExpediente.getDixonGrupo() == DixonGrupoExpediente.OBSERVADO) {
            VBox andraObs = new VBox(AndraComponentes.andraEtiqueta("Observación", "obs-titulo"), AndraComponentes.andraParrafo(andraExpediente.getDixonObservacion(), "obs-texto"));
            andraObs.getStyleClass().add("obs");
            andraDerecha.getChildren().add(andraObs);
        }
        GridPane andraRejilla = new GridPane();
        andraRejilla.setHgap(24);
        ColumnConstraints andraRestriccionColumna1 = new ColumnConstraints();
        andraRestriccionColumna1.setPercentWidth(50);
        ColumnConstraints andraRestriccionColumna2 = new ColumnConstraints();
        andraRestriccionColumna2.setPercentWidth(50);
        andraRejilla.getColumnConstraints().addAll(andraRestriccionColumna1, andraRestriccionColumna2);
        andraRejilla.add(andraIzquierda, 0, 0);
        andraRejilla.add(andraDerecha, 1, 0);
        GridPane.setValignment(andraDerecha, javafx.geometry.VPos.TOP);
        VBox andraCuerpo = new VBox(14, andraFichas, andraRejilla);
        if (andraPago != null && andraPago.getDixonTramite() != null && andraPago.getDixonTramite().dixonGeneraDocumento() && andraPago.dixonEsPagado()) {
            andraCuerpo.getChildren().add(new HBox(AndraComponentes.andraBotonSm("Ver documento generado", "btn-tertiary", "fth-file-text", andraEvento -> andraAccionesPago.andraDocumento(andraPago.getDixonCodigo()))));
        }
        List<Node> andraPie = new ArrayList<>();
        Button andraObservar = AndraComponentes.andraBoton("Observar", "btn-danger", null, andraEvento -> andraObservar(andraExpediente, andraAlCambiar));
        andraObservar.setDisable(andraExpediente.getDixonGrupo() != DixonGrupoExpediente.TRAMITE);
        andraPie.add(andraObservar);
        if (andraExpediente.getDixonGrupo() == DixonGrupoExpediente.LISTO && !andraExpediente.dixonEsEntregado()) {
            andraPie.add(AndraComponentes.andraBoton("Marcar entregado", "btn-secondary", null, andraEvento -> {
                andraAdministrador.dixonEntregar(andraExpediente.getDixonCodigo());
                andraNotificador.andraCerrarModal();
                andraAlCambiar.run();
                andraNotificador.andraExito("Entrega registrada.");
            }));
        }
        boolean andraPuede = DixonAdministradorServicio.dixonPuedeEmitir(andraExpediente);
        Button andraEmitir = AndraComponentes.andraBoton(andraPuede ? "Emitir y marcar listo para recoger" : andraPago != null && !andraPago.dixonEsPagado() ? "Esperando pago" : "Ya atendido",
                "btn-primary", null, andraEvento -> {
                    try {
                        andraAdministrador.dixonEmitir(andraExpediente.getDixonCodigo());
                        andraNotificador.andraCerrarModal();
                        andraAlCambiar.run();
                        andraNotificador.andraExito(andraExpediente.getDixonCodigo() + " emitido y listo para recoger.");
                    } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                        andraNotificador.andraError(andraNegocioExcepcion.getMessage());
                    }
                });
        andraEmitir.setDisable(!andraPuede);
        andraPie.add(andraEmitir);
        andraNotificador.andraModal(andraExpediente.getDixonTitulo(), andraCuerpo, true, andraPie.toArray(new Node[0]));
    }

    private void andraObservar(DixonExpediente andraExpediente, Runnable andraAlCambiar) {
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<TextArea> andraMotivo = andraFormulario.andraAgregar(AndraFormulario.andraArea("Motivo de la observación", "Ej. Falta adjuntar la copia del DNI legible."))
                .andraRequerido("Explica qué debe corregir el solicitante.").andraMinimo(15);
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo(andraExpediente.getDixonCodigo() + " · " + andraExpediente.getDixonTitulo() + ". El solicitante verá este motivo y podrá subsanar.", "muted", "small"), andraMotivo.andraNodo());
        andraNotificador.andraModal("Observar trámite", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Enviar observación", "btn-primary", null, andraEvento -> {
                    if (!andraFormulario.andraValidar()) {
                        return;
                    }
                    try {
                        andraAdministrador.dixonObservar(andraExpediente.getDixonCodigo(), andraMotivo.andraControl().getText());
                        andraNotificador.andraCerrarModal();
                        andraAlCambiar.run();
                        andraNotificador.andraExito("Observación enviada al solicitante.");
                    } catch (DixonNegocioExcepcion andraNegocioExcepcion) {
                        andraNotificador.andraError(andraNegocioExcepcion.getMessage());
                    }
                }));
    }

    public void andraReembolsar(DixonPago andraPago, Runnable andraAlCambiar) {
        andraNotificador.andraConfirmar("¿Reembolsar este pago?", andraPago.getDixonConcepto() + " · " + DixonMontos.dixonSoles(andraPago.dixonObtenerTotal()) + " a " + andraPago.getDixonPersona().getDixonNombre()
                + ". Tesorería devolverá el monto por el mismo medio de pago.", "Reembolsar", true, () -> {
            andraAdministrador.dixonReembolsar(andraPago.getDixonCodigo());
            andraAlCambiar.run();
            andraNotificador.andraExito("Reembolso registrado.");
        });
    }

    public void andraAprobarReserva(DixonReserva andraReserva, Runnable andraAlCambiar) {
        andraAdministrador.dixonAprobarReserva(andraReserva.getDixonId());
        andraAlCambiar.run();
        andraNotificador.andraExito("Reserva aprobada.");
    }

    public void andraRechazarReserva(DixonReserva andraReserva, Runnable andraAlCambiar) {
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<TextArea> andraMotivo = andraFormulario.andraAgregar(AndraFormulario.andraArea("Motivo", null)).andraRequerido("Indica el motivo del rechazo.").andraMinimo(10);
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraLiteralParrafo(andraReserva.getDixonEspacio().getDixonNombre() + " · " + DixonFechas.dixonDmy(andraReserva.getDixonFecha()) + " " + andraReserva.getDixonHorario(), "muted", "small"), andraMotivo.andraNodo());
        andraNotificador.andraModal("Rechazar reserva", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Rechazar", "btn-primary", null, andraEvento -> {
                    if (andraFormulario.andraValidar()) {
                        andraAdministrador.dixonRechazarReserva(andraReserva.getDixonId(), andraMotivo.andraControl().getText());
                        andraNotificador.andraCerrarModal();
                        andraAlCambiar.run();
                        andraNotificador.andraExito("Reserva rechazada.");
                    }
                }));
    }

    public void andraAprobarPrestamo(DixonPrestamo andraPrestamo, Runnable andraAlCambiar) {
        andraAdministrador.dixonAprobarPrestamo(andraPrestamo.getDixonId());
        andraAlCambiar.run();
        andraNotificador.andraExito("Préstamo aprobado.");
    }

    public void andraRechazarPrestamo(DixonPrestamo andraPrestamo, Runnable andraAlCambiar) {
        andraNotificador.andraConfirmar("¿Rechazar este préstamo?", andraPrestamo.getDixonNombre() + " (" + andraPrestamo.getDixonCodigo() + ").", "Rechazar", true, () -> {
            andraAdministrador.dixonRechazarPrestamo(andraPrestamo.getDixonId());
            andraAlCambiar.run();
            andraNotificador.andraExito("Préstamo rechazado.");
        });
    }

    public void andraDevolverPrestamo(DixonPrestamo andraPrestamo, Runnable andraAlCambiar) {
        andraAdministrador.dixonDevolverPrestamo(andraPrestamo.getDixonId());
        andraAlCambiar.run();
        andraNotificador.andraExito("Devolución registrada.");
    }

    public void andraDevolverCustodia(Runnable andraAlCambiar) {
        andraAdministrador.dixonDevolverCustodia();
        andraAlCambiar.run();
        andraNotificador.andraExito("Devolución registrada.");
    }

    public DixonDocumentoGeneradoServicio andraGenerador() {
        return andraGenerador;
    }
}
