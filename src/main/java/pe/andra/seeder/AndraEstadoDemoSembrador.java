package pe.andra.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.config.AndraJsonRecursos;
import pe.andra.model.*;
import pe.andra.repository.*;
import pe.andra.util.AndraFechas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AndraEstadoDemoSembrador {

    private static final Pattern ANDRA_FECHA_DMY = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");

    private final AndraJsonRecursos andraJson;
    private final AndraSedeRepositorio andraSedes;
    private final AndraEspacioRepositorio andraEspacios;
    private final AndraEquipoRepositorio andraEquipos;
    private final AndraTramiteRepositorio andraTramites;
    private final AndraPersonaRepositorio andraPersonas;
    private final AndraPagoRepositorio andraPagos;
    private final AndraExpedienteRepositorio andraExpedientes;
    private final AndraReservaRepositorio andraReservas;
    private final AndraPrestamoRepositorio andraPrestamos;
    private final AndraPrestamoActivoRepositorio andraActivos;
    private final AndraFavoritoRepositorio andraFavoritos;
    private final AndraDocumentoPersonalRepositorio andraDocumentosPersonales;
    private final AndraNotificacionRepositorio andraNotificaciones;
    private final AndraMatriculaRepositorio andraMatriculas;
    private final AndraMensajeEnviadoRepositorio andraMensajes;
    private final AndraConsultaAsesorRepositorio andraConsultas;
    private final AndraCambioPendienteRepositorio andraCambios;
    private final AndraUsoCarneRepositorio andraUsos;
    private final AndraAjustesRepositorio andraAjustes;

    private long andraDesfaseDias;

    @Transactional
    public boolean andraSembrar() {
        if (andraAjustes.existsById(1L)) {
            return false;
        }
        andraCargar(false);
        return true;
    }

    @Transactional
    public void andraRestablecer() {
        andraMensajes.deleteAllInBatch();
        andraConsultas.deleteAll();
        andraMatriculas.deleteAllInBatch();
        andraExpedientes.deleteAll();
        andraReservas.deleteAllInBatch();
        andraPrestamos.deleteAllInBatch();
        andraActivos.deleteAllInBatch();
        andraFavoritos.deleteAllInBatch();
        andraDocumentosPersonales.deleteAllInBatch();
        andraNotificaciones.deleteAllInBatch();
        andraCambios.deleteAllInBatch();
        andraUsos.deleteAllInBatch();
        andraPagos.deleteAll();
        andraExpedientes.flush();
        andraPagos.flush();
        andraEquipos.findAll().forEach(andraEquipo -> andraEquipo.setAndraSaldo(andraEquipo.getAndraUnidadesIniciales()));
        andraCargar(true);
    }

    private void andraCargar(boolean andraConservar) {
        JsonNode andraNodo = andraJson.andraDemo();
        LocalDate andraReferencia = LocalDate.parse(andraNodo.path("fechaReferencia").asText());
        andraDesfaseDias = ChronoUnit.DAYS.between(andraReferencia, AndraFechas.andraHoy());
        AndraPersona andraEstudianteActual = andraPersonas.findFirstByAndraPrincipalTrue().orElseThrow();

        for (JsonNode andraNodo2 : andraNodo.path("pagos")) {
            List<AndraElementoPago> andraElementos = new ArrayList<>();
            for (JsonNode andraNodo3 : andraNodo2.path("items")) {
                andraElementos.add(new AndraElementoPago(andraNodo3.get(0).asText(), andraNodo3.get(1).asInt(), new BigDecimal(andraNodo3.get(2).asText())));
            }
            andraPagos.save(AndraPago.builder().andraCodigo(andraNodo2.path("id").asText()).andraPersona(andraPersona(andraNodo2.path("user").asText()))
                    .andraTipo(AndraTipoPago.andraDesdeClave(andraNodo2.path("tipo").asText()))
                    .andraTramite(andraTramite(andraNodo2.path("tupa").asText(null))).andraConcepto(andraNodo2.path("concepto").asText())
                    .andraEstado(AndraEstadoPago.valueOf(andraNodo2.path("estado").asText().toUpperCase()))
                    .andraCreado(andraMomento(andraNodo2.path("creado").asText(null))).andraVence(andraFecha(andraNodo2.path("vence").asText(null)))
                    .andraPagado(andraMomento(andraNodo2.path("pagado").asText(null)))
                    .andraMetodo(AndraMetodoPago.andraDesdeClave(andraNodo2.path("metodo").asText(null)))
                    .andraOperacion(andraNodo2.path("op").asText(null)).andraBoleta(andraNodo2.path("boleta").asText(null))
                    .andraSede(andraSede(andraNodo2.path("sede").asText("central"))).andraElementos(andraElementos).build());
        }

        List<JsonNode> andraOtrosExp = andraLista(andraNodo.path("others").path("sols"));
        List<JsonNode> andraPropiosExp = andraLista(andraNodo.path("sols"));
        for (int andraIndice = andraOtrosExp.size() - 1; andraIndice >= 0; andraIndice--) {
            andraGuardarExpediente(andraOtrosExp.get(andraIndice), null, true);
        }
        for (int andraIndice = andraPropiosExp.size() - 1; andraIndice >= 0; andraIndice--) {
            andraGuardarExpediente(andraPropiosExp.get(andraIndice), andraEstudianteActual, false);
        }

        List<JsonNode> andraOtrasRes = andraLista(andraNodo.path("others").path("reservas"));
        List<JsonNode> andraPropiasRes = andraLista(andraNodo.path("reservas"));
        for (int andraIndice = andraOtrasRes.size() - 1; andraIndice >= 0; andraIndice--) {
            andraGuardarReserva(andraOtrasRes.get(andraIndice), null);
        }
        for (int andraIndice = andraPropiasRes.size() - 1; andraIndice >= 0; andraIndice--) {
            andraGuardarReserva(andraPropiasRes.get(andraIndice), andraEstudianteActual);
        }

        List<JsonNode> andraOtrosHist = andraLista(andraNodo.path("others").path("hist"));
        List<JsonNode> andraPropiosHist = andraLista(andraNodo.path("hist"));
        for (int andraIndice = andraOtrosHist.size() - 1; andraIndice >= 0; andraIndice--) {
            andraGuardarPrestamo(andraOtrosHist.get(andraIndice), null, true);
        }
        for (int andraIndice = andraPropiosHist.size() - 1; andraIndice >= 0; andraIndice--) {
            andraGuardarPrestamo(andraPropiosHist.get(andraIndice), andraEstudianteActual, false);
        }

        JsonNode andraNodo4 = andraNodo.path("loan");
        andraActivos.save(AndraPrestamoActivo.builder().andraCodigo(andraNodo4.path("code").asText()).andraPersona(andraEstudianteActual)
                .andraEquipo(andraNodo4.path("item").asText()).andraRetiro(andraNodo4.path("retiro").asText())
                .andraLimite(LocalDateTime.now(AndraFechas.andraReloj()).plusHours(2).plusMinutes(15).withSecond(0).withNano(0))
                .andraRenovado(andraNodo4.path("renewed").asBoolean()).andraIncidencia(andraNodo4.path("incident").asText(null))
                .andraLugar(andraNodo4.path("place").asText()).andraDevuelto(false).build());

        andraFavoritos(andraNodo.path("favDocs"), AndraTipoFavorito.DOCUMENTO);
        andraFavoritos(andraNodo.path("myFav"), AndraTipoFavorito.MI_DOCUMENTO);
        andraFavoritos(andraNodo.path("favEq"), AndraTipoFavorito.EQUIPO);
        andraFavoritos(andraNodo.path("favSpaces"), AndraTipoFavorito.ESPACIO);

        for (JsonNode andraNodo5 : andraNodo.path("myDocs")) {
            andraDocumentosPersonales.save(AndraDocumentoPersonal.builder().andraCodigo(andraNodo5.path("id").asText())
                    .andraNombre(andraNodo5.path("name").asText()).build());
        }

        andraNotificaciones(andraNodo.path("notifs"), AndraRol.ESTUDIANTE);
        andraNotificaciones(andraNodo.path("adminNotifs"), AndraRol.ADMINISTRADOR);

        JsonNode andraNodo6 = andraNodo.path("matricula");
        andraMatriculas.save(AndraMatricula.builder().andraPeriodo(andraNodo6.path("periodo").asText())
                .andraEstado("confirmada".equals(andraNodo6.path("estado").asText()) ? AndraEstadoMatricula.CONFIRMADA : AndraEstadoMatricula.PENDIENTE)
                .andraPago(andraPagos.findByAndraCodigo(andraNodo6.path("pago").asText()).orElseThrow()).build());

        List<JsonNode> andraListaUsos = andraLista(andraNodo.path("usosCarne"));
        for (int andraIndice = andraListaUsos.size() - 1; andraIndice >= 0; andraIndice--) {
            JsonNode andraNodo7 = andraListaUsos.get(andraIndice);
            andraUsos.save(AndraUsoCarne.builder().andraLugar(andraNodo7.path("lugar").asText())
                    .andraFecha(AndraFechas.andraHoy().minusDays(andraNodo7.path("diasAtras").asInt()).atTime(LocalTime.parse(andraNodo7.path("hora").asText())))
                    .andraConcedido(andraNodo7.path("ok").asBoolean()).build());
        }

        JsonNode andraContexto = andraNodo.path("ctx");
        JsonNode andraPref = andraNodo.path("preferenciasAviso");
        String andraEscuela = andraContexto.path("fac").asText("all");
        AndraAjustes andraAjustes2 = andraAjustes.findById(1L).orElseGet(() -> AndraAjustes.builder().andraId(1L).andraMaximizada(false).build());
        if (!andraConservar || andraAjustes2.getAndraIdioma() == null) {
            andraAjustes2.setAndraIdioma(andraNodo.path("idioma").asText("es"));
        }
        andraAjustes2.setAndraSede(andraContexto.path("sede").asText("central"));
        andraAjustes2.setAndraEscuela(AndraAjustes.ANDRA_TODAS.equals(andraEscuela) ? null : andraEscuela);
        andraAjustes2.setAndraAvisoWhatsapp(andraPref.path("whatsapp").asBoolean(true));
        andraAjustes2.setAndraAvisoCorreo(andraPref.path("correo").asBoolean(true));
        andraAjustes2.setAndraAvisoAplicacion(andraPref.path("app").asBoolean(true));
        if (!andraConservar) {
            andraAjustes2.setAndraSesion(null);
            andraAjustes2.setAndraSesionDesde(null);
            andraAjustes2.setAndraReducirMovimiento(false);
        }
        andraAjustes2.setAndraVersion(andraNodo.path("v").asInt(5));
        andraAjustes.save(andraAjustes2);
    }

    private void andraGuardarExpediente(JsonNode andraNodo, AndraPersona andraPropietario, boolean andraDesplazar) {
        AndraPersona andraPersona = andraPropietario != null ? andraPropietario : andraPersona(andraNodo.path("user").asText());
        AndraExpediente andraExpediente = AndraExpediente.builder().andraCodigo(andraNodo.path("id").asText()).andraPersona(andraPersona)
                .andraSede(andraNodo.has("sede") ? andraSede(andraNodo.path("sede").asText()) : andraPersona.getAndraSede())
                .andraClase(AndraClaseExpediente.valueOf(andraNodo.path("kind").asText("doc").toUpperCase()))
                .andraTramite(andraTramite(andraNodo.path("tupa").asText(null)))
                .andraPago(andraNodo.hasNonNull("pago") ? andraPagos.findByAndraCodigo(andraNodo.path("pago").asText()).orElse(null) : null)
                .andraGrupo(AndraGrupoExpediente.andraDesdeClave(andraNodo.path("group").asText()))
                .andraIcono(andraNodo.path("icon").asText("doc")).andraRotuloIcono(andraNodo.path("iconLabel").asText(""))
                .andraTono(andraNodo.path("tone").asText("neutral")).andraEstado(andraNodo.path("status").asText())
                .andraTitulo(andraNodo.path("title").asText()).andraDescripcion(andraNodo.path("desc").asText(null))
                .andraPaso(andraNodo.path("step").asInt(1)).andraObservacion(andraNodo.path("obs").asText(null))
                .andraDestacado(andraNodo.path("flag").asBoolean(false)).andraNota(andraNodo.path("note").asText(null))
                .andraFecha(andraMomento(andraNodo.path("fecha").asText(null))).build();
        for (JsonNode andraNodo2 : andraNodo.path("meta")) {
            String andraValor = andraNodo2.get(1).asText();
            andraExpediente.getAndraDatos().add(new AndraDatoExpediente(andraNodo2.get(0).asText(), andraDesplazar ? andraDesplazarTexto(andraValor) : andraValor));
        }
        andraNodo.path("tags").forEach(andraNodo3 -> andraExpediente.getAndraEtiquetas().add(andraNodo3.asText()));
        for (JsonNode andraNodo4 : andraNodo.path("acts")) {
            String andraAccion = andraNodo4.get(1).asText();
            String andraTexto = "route-sol".equals(andraAccion) ? "Seguimiento en vivo" : andraNodo4.get(2).asText();
            andraExpediente.getAndraAcciones().add(new AndraAccionExpediente(andraNodo4.get(0).asText(), andraAccion, andraTexto));
        }
        JsonNode andraSol = andraNodo.path("solicitante");
        if (!andraSol.isMissingNode()) {
            andraExpediente.setAndraSolicitante(AndraDatosSolicitante.builder().andraTipo(andraSol.path("tipo").asText()).andraNombre(andraSol.path("nombre").asText())
                    .andraDni(andraSol.path("dni").asText()).andraCodigo(andraSol.path("codigo").asText())
                    .andraEscuela(andraSol.path("fac").asText(null)).andraCorreo(andraSol.path("correo").asText()).build());
        }
        andraExpedientes.save(andraExpediente);
    }

    private void andraGuardarReserva(JsonNode andraNodo, AndraPersona andraPropietario) {
        AndraEspacio andraEspacio = andraEspacios.findFirstByAndraNombre(andraNodo.path("space").asText()).orElseThrow();
        andraReservas.save(AndraReserva.builder().andraCodigo(andraNodo.path("id").asText())
                .andraPersona(andraPropietario != null ? andraPropietario : andraPersona(andraNodo.path("user").asText()))
                .andraEspacio(andraEspacio).andraFecha(andraFecha(andraNodo.path("date").asText())).andraHorario(andraNodo.path("hora").asText())
                .andraEstado(AndraEstadoReserva.andraDesdeEtiqueta(andraNodo.path("status").asText())).andraMotivo(andraNodo.path("motivo").asText())
                .andraAsistentes(andraNodo.path("asist").asInt()).andraNota(andraNodo.path("nota").asText(null)).build());
    }

    private void andraGuardarPrestamo(JsonNode andraAlto, AndraPersona andraPropietario, boolean andraDesplazar) {
        String andraDetalle = andraDesplazar ? andraDesplazarTexto(andraAlto.path("sub").asText()) : andraAlto.path("sub").asText();
        String andraSede = andraDetalle.contains("Sede Norte") ? "norte" : andraDetalle.contains("Sede Sur") ? "sur" : "central";
        AndraEquipo andraEquipo = andraEquipos.findAll().stream().filter(andraPosicionX -> andraAlto.path("name").asText().startsWith(andraPosicionX.getAndraNombre()))
                .findFirst().orElse(null);
        andraPrestamos.save(AndraPrestamo.builder().andraCodigo(andraAlto.path("code").asText()).andraClave(andraAlto.path("id").asText())
                .andraPersona(andraPropietario != null ? andraPropietario : andraPersona(andraAlto.path("user").asText()))
                .andraEquipo(andraEquipo).andraNombre(andraAlto.path("name").asText()).andraDetalle(andraDetalle)
                .andraEstado(AndraEstadoPrestamo.andraDesdeEtiqueta(andraAlto.path("status").asText())).andraSede(andraSede(andraSede)).build());
    }

    private void andraFavoritos(JsonNode andraCodigos, AndraTipoFavorito andraTipo) {
        andraCodigos.forEach(andraNodo -> andraFavoritos.save(AndraFavorito.builder().andraTipo(andraTipo).andraCodigo(andraNodo.asText()).build()));
    }

    private void andraNotificaciones(JsonNode andraLista, AndraRol andraRol) {
        List<JsonNode> andraLista2 = andraLista(andraLista);
        LocalDateTime andraBase = AndraFechas.andraAhora().minusHours(andraLista2.size());
        for (int andraIndice = andraLista2.size() - 1; andraIndice >= 0; andraIndice--) {
            JsonNode andraNodo = andraLista2.get(andraIndice);
            andraNotificaciones.save(AndraNotificacion.builder().andraDestinatario(andraRol).andraTexto(andraNodo.path("text").asText())
                    .andraRuta(andraNodo.path("to").asText()).andraLeida(andraNodo.path("read").asBoolean())
                    .andraCreada(andraBase.plusHours(andraLista2.size() - andraIndice)).build());
        }
    }

    private List<JsonNode> andraLista(JsonNode andraNodo) {
        List<JsonNode> andraLista = new ArrayList<>();
        andraNodo.forEach(andraLista::add);
        return andraLista;
    }

    private AndraPersona andraPersona(String andraCodigo) {
        return andraPersonas.findByAndraCodigo(andraCodigo).orElseThrow(() -> new IllegalStateException("Persona " + andraCodigo));
    }

    private AndraSede andraSede(String andraCodigo) {
        return andraSedes.findByAndraCodigo(andraCodigo).orElseThrow();
    }

    private AndraTramite andraTramite(String andraCodigo) {
        return andraCodigo == null || andraCodigo.isBlank() ? null : andraTramites.findByAndraCodigo(andraCodigo).orElse(null);
    }

    private LocalDate andraFecha(String andraTexto) {
        LocalDate andraFecha = AndraFechas.andraLeerFecha(andraTexto);
        return andraFecha == null ? null : andraFecha.plusDays(andraDesfaseDias);
    }

    private LocalDateTime andraMomento(String andraTexto) {
        LocalDateTime andraFechaHora = AndraFechas.andraLeerMomento(andraTexto);
        return andraFechaHora == null ? null : andraFechaHora.plusDays(andraDesfaseDias);
    }

    private String andraDesplazarTexto(String andraTexto) {
        if (andraTexto == null || andraDesfaseDias == 0) {
            return andraTexto;
        }
        Matcher andraCoincidencia = ANDRA_FECHA_DMY.matcher(andraTexto);
        StringBuilder andraTexto2 = new StringBuilder();
        while (andraCoincidencia.find()) {
            LocalDate andraFecha = LocalDate.of(Integer.parseInt(andraCoincidencia.group(3)), Integer.parseInt(andraCoincidencia.group(2)),
                    Integer.parseInt(andraCoincidencia.group(1))).plusDays(andraDesfaseDias);
            andraCoincidencia.appendReplacement(andraTexto2, AndraFechas.andraDmy(andraFecha));
        }
        andraCoincidencia.appendTail(andraTexto2);
        return andraTexto2.toString();
    }
}
