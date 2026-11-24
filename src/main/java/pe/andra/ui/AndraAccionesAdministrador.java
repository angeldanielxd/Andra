package pe.andra.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.service.*;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraMontos;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndraAccionesAdministrador {

    private final AndraNotificador andraNotificador;
    private final AndraAdministradorServicio andraAdministrador;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraAccionesPago andraAccionesPago;
    private final AndraDocumentoGeneradoServicio andraGenerador;

    public AndraDatosSolicitante andraSolicitante(AndraExpediente andraExpediente) {
        if (andraExpediente.getAndraSolicitante() != null && andraExpediente.getAndraSolicitante().getAndraNombre() != null) {
            return andraExpediente.getAndraSolicitante();
        }
        if (andraExpediente.getAndraPago() != null && andraExpediente.getAndraPago().getAndraSolicitante() != null && andraExpediente.getAndraPago().getAndraSolicitante().getAndraNombre() != null) {
            return andraExpediente.getAndraPago().getAndraSolicitante();
        }
        AndraPersona andraPersona = andraExpediente.getAndraPersona();
        return AndraDatosSolicitante.builder().andraTipo(andraPersona.getAndraTipo()).andraNombre(andraPersona.getAndraNombre()).andraDni(andraPersona.getAndraDni()).andraCodigo(andraPersona.getAndraCodigo())
                .andraEscuela(andraPersona.getAndraEscuela() == null ? null : andraPersona.getAndraEscuela().getAndraCodigo()).andraCorreo(andraPersona.getAndraCorreo()).build();
    }

    public void andraRevisar(String andraCodigo, Runnable andraAlCambiar) {
        AndraExpediente andraExpediente = andraAdministrador.andraExpediente(andraCodigo);
        AndraPago andraPago = andraExpediente.getAndraPago();
        AndraDatosSolicitante andraSol = andraSolicitante(andraExpediente);
        FlowPane andraFichas = AndraComponentes.andraFichas(AndraComponentes.andraPildora(andraExpediente.getAndraEstado(), andraExpediente.getAndraTono()), AndraComponentes.andraLiteral(andraExpediente.getAndraCodigo(), "tag"),
                AndraComponentes.andraDistintivo("Sede " + andraExpediente.getAndraSede().getAndraNombre()));
        List<Object> andraDatos = new ArrayList<>(List.of("Nombre", AndraComponentes.andraLiteralParrafo(andraSol.getAndraNombre(), "dd"), "Tipo", andraSol.getAndraTipo(),
                "DNI", AndraComponentes.andraLiteral(andraSol.getAndraDni() == null ? "—" : andraSol.getAndraDni(), "dd"), "Código", AndraComponentes.andraLiteral(andraSol.getAndraCodigo() == null ? "—" : andraSol.getAndraCodigo(), "dd")));
        if (andraSol.getAndraEscuela() != null && andraCatalogo.andraEscuela(andraSol.getAndraEscuela()).isPresent()) {
            andraDatos.add("Escuela");
            andraDatos.add(AndraComponentes.andraLiteralParrafo("EP " + andraCatalogo.andraNombreEscuela(andraSol.getAndraEscuela()), "dd"));
        }
        if (andraSol.getAndraCorreo() != null) {
            andraDatos.add("Correo");
            andraDatos.add(AndraComponentes.andraLiteral(andraSol.getAndraCorreo(), "dd"));
        }
        VBox andraIzquierda = new VBox(8, AndraComponentes.andraMayus("Solicitante", "label-caps"), AndraComponentes.andraKv(andraDatos.toArray()));
        VBox andraDerecha = new VBox(8, AndraComponentes.andraMayus("Pago", "label-caps"));
        if (andraPago != null) {
            List<Object> andraPagoDatos = new ArrayList<>(List.of("Orden", AndraComponentes.andraLiteral(andraPago.getAndraCodigo(), "dd"), "Monto", AndraMontos.andraSoles(andraPago.andraObtenerTotal()),
                    "Estado", andraPago.andraEsPagado() ? AndraComponentes.andraPildora("Pagado · " + andraPago.getAndraMetodo().andraObtenerEtiqueta(), "success") : AndraComponentes.andraPildora("Pendiente", "warning")));
            if (andraPago.getAndraBoleta() != null) {
                andraPagoDatos.add("Comprobante");
                andraPagoDatos.add(AndraComponentes.andraLiteral(andraPago.getAndraBoleta(), "dd"));
            }
            andraDerecha.getChildren().add(AndraComponentes.andraKv(andraPagoDatos.toArray()));
        } else {
            andraDerecha.getChildren().add(AndraComponentes.andraParrafo("Este trámite no tiene costo.", "muted", "small"));
        }
        if (andraExpediente.getAndraObservacion() != null && andraExpediente.getAndraGrupo() == AndraGrupoExpediente.OBSERVADO) {
            VBox andraObs = new VBox(AndraComponentes.andraEtiqueta("Observación", "obs-titulo"), AndraComponentes.andraParrafo(andraExpediente.getAndraObservacion(), "obs-texto"));
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
        if (andraPago != null && andraPago.getAndraTramite() != null && andraPago.getAndraTramite().andraGeneraDocumento() && andraPago.andraEsPagado()) {
            andraCuerpo.getChildren().add(new HBox(AndraComponentes.andraBotonSm("Ver documento generado", "btn-tertiary", "fth-file-text", andraEvento -> andraAccionesPago.andraDocumento(andraPago.getAndraCodigo()))));
        }
        List<Node> andraPie = new ArrayList<>();
        Button andraObservar = AndraComponentes.andraBoton("Observar", "btn-danger", null, andraEvento -> andraObservar(andraExpediente, andraAlCambiar));
        andraObservar.setDisable(andraExpediente.getAndraGrupo() != AndraGrupoExpediente.TRAMITE);
        andraPie.add(andraObservar);
        if (andraExpediente.getAndraGrupo() == AndraGrupoExpediente.LISTO && !andraExpediente.andraEsEntregado()) {
            andraPie.add(AndraComponentes.andraBoton("Marcar entregado", "btn-secondary", null, andraEvento -> {
                andraAdministrador.andraEntregar(andraExpediente.getAndraCodigo());
                andraNotificador.andraCerrarModal();
                andraAlCambiar.run();
                andraNotificador.andraExito("Entrega registrada.");
            }));
        }
        boolean andraPuede = AndraAdministradorServicio.andraPuedeEmitir(andraExpediente);
        Button andraEmitir = AndraComponentes.andraBoton(andraPuede ? "Emitir y marcar listo para recoger" : andraPago != null && !andraPago.andraEsPagado() ? "Esperando pago" : "Ya atendido",
                "btn-primary", null, andraEvento -> {
                    try {
                        andraAdministrador.andraEmitir(andraExpediente.getAndraCodigo());
                        andraNotificador.andraCerrarModal();
                        andraAlCambiar.run();
                        andraNotificador.andraExito(andraExpediente.getAndraCodigo() + " emitido y listo para recoger.");
                    } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
                        andraNotificador.andraError(andraNegocioExcepcion.getMessage());
                    }
                });
        andraEmitir.setDisable(!andraPuede);
        andraPie.add(andraEmitir);
        andraNotificador.andraModal(andraExpediente.getAndraTitulo(), andraCuerpo, true, andraPie.toArray(new Node[0]));
    }

    private void andraObservar(AndraExpediente andraExpediente, Runnable andraAlCambiar) {
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<TextArea> andraMotivo = andraFormulario.andraAgregar(AndraFormulario.andraArea("Motivo de la observación", "Ej. Falta adjuntar la copia del DNI legible."))
                .andraRequerido("Explica qué debe corregir el solicitante.").andraMinimo(15);
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraParrafo(andraExpediente.getAndraCodigo() + " · " + andraExpediente.getAndraTitulo() + ". El solicitante verá este motivo y podrá subsanar.", "muted", "small"), andraMotivo.andraNodo());
        andraNotificador.andraModal("Observar trámite", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Enviar observación", "btn-primary", null, andraEvento -> {
                    if (!andraFormulario.andraValidar()) {
                        return;
                    }
                    try {
                        andraAdministrador.andraObservar(andraExpediente.getAndraCodigo(), andraMotivo.andraControl().getText());
                        andraNotificador.andraCerrarModal();
                        andraAlCambiar.run();
                        andraNotificador.andraExito("Observación enviada al solicitante.");
                    } catch (AndraNegocioExcepcion andraNegocioExcepcion) {
                        andraNotificador.andraError(andraNegocioExcepcion.getMessage());
                    }
                }));
    }

    public void andraReembolsar(AndraPago andraPago, Runnable andraAlCambiar) {
        andraNotificador.andraConfirmar("¿Reembolsar este pago?", andraPago.getAndraConcepto() + " · " + AndraMontos.andraSoles(andraPago.andraObtenerTotal()) + " a " + andraPago.getAndraPersona().getAndraNombre()
                + ". Tesorería devolverá el monto por el mismo medio de pago.", "Reembolsar", true, () -> {
            andraAdministrador.andraReembolsar(andraPago.getAndraCodigo());
            andraAlCambiar.run();
            andraNotificador.andraExito("Reembolso registrado.");
        });
    }

    public void andraAprobarReserva(AndraReserva andraReserva, Runnable andraAlCambiar) {
        andraAdministrador.andraAprobarReserva(andraReserva.getAndraId());
        andraAlCambiar.run();
        andraNotificador.andraExito("Reserva aprobada.");
    }

    public void andraRechazarReserva(AndraReserva andraReserva, Runnable andraAlCambiar) {
        AndraFormulario andraFormulario = new AndraFormulario();
        AndraCampo<TextArea> andraMotivo = andraFormulario.andraAgregar(AndraFormulario.andraArea("Motivo", null)).andraRequerido("Indica el motivo del rechazo.").andraMinimo(10);
        VBox andraCuerpo = new VBox(14, AndraComponentes.andraLiteralParrafo(andraReserva.getAndraEspacio().getAndraNombre() + " · " + AndraFechas.andraDmy(andraReserva.getAndraFecha()) + " " + andraReserva.getAndraHorario(), "muted", "small"), andraMotivo.andraNodo());
        andraNotificador.andraModal("Rechazar reserva", andraCuerpo, false, AndraComponentes.andraBoton("Cancelar", "btn-secondary", null, andraEvento -> andraNotificador.andraCerrarModal()),
                AndraComponentes.andraBoton("Rechazar", "btn-primary", null, andraEvento -> {
                    if (andraFormulario.andraValidar()) {
                        andraAdministrador.andraRechazarReserva(andraReserva.getAndraId(), andraMotivo.andraControl().getText());
                        andraNotificador.andraCerrarModal();
                        andraAlCambiar.run();
                        andraNotificador.andraExito("Reserva rechazada.");
                    }
                }));
    }

    public void andraAprobarPrestamo(AndraPrestamo andraPrestamo, Runnable andraAlCambiar) {
        andraAdministrador.andraAprobarPrestamo(andraPrestamo.getAndraId());
        andraAlCambiar.run();
        andraNotificador.andraExito("Préstamo aprobado.");
    }

    public void andraRechazarPrestamo(AndraPrestamo andraPrestamo, Runnable andraAlCambiar) {
        andraNotificador.andraConfirmar("¿Rechazar este préstamo?", andraPrestamo.getAndraNombre() + " (" + andraPrestamo.getAndraCodigo() + ").", "Rechazar", true, () -> {
            andraAdministrador.andraRechazarPrestamo(andraPrestamo.getAndraId());
            andraAlCambiar.run();
            andraNotificador.andraExito("Préstamo rechazado.");
        });
    }

    public void andraDevolverPrestamo(AndraPrestamo andraPrestamo, Runnable andraAlCambiar) {
        andraAdministrador.andraDevolverPrestamo(andraPrestamo.getAndraId());
        andraAlCambiar.run();
        andraNotificador.andraExito("Devolución registrada.");
    }

    public void andraDevolverCustodia(Runnable andraAlCambiar) {
        andraAdministrador.andraDevolverCustodia();
        andraAlCambiar.run();
        andraNotificador.andraExito("Devolución registrada.");
    }

    public AndraDocumentoGeneradoServicio andraGenerador() {
        return andraGenerador;
    }
}
