package pe.andra.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.config.AndraJsonRecursos;
import pe.andra.model.*;
import pe.andra.repository.*;
import pe.andra.util.DixonFechas;

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
public class DixonEstadoDemoSembrador {

    private static final Pattern DIXON_FECHA_DMY = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");

    private final AndraJsonRecursos dixonJson;
    private final DixonSedeRepositorio dixonSedes;
    private final DixonEspacioRepositorio dixonEspacios;
    private final DixonEquipoRepositorio dixonEquipos;
    private final DixonTramiteRepositorio dixonTramites;
    private final DixonPersonaRepositorio dixonPersonas;
    private final DixonPagoRepositorio dixonPagos;
    private final DixonExpedienteRepositorio dixonExpedientes;
    private final DixonReservaRepositorio dixonReservas;
    private final DixonPrestamoRepositorio dixonPrestamos;
    private final DixonPrestamoActivoRepositorio dixonActivos;
    private final DixonFavoritoRepositorio dixonFavoritos;
    private final DixonDocumentoPersonalRepositorio dixonDocumentosPersonales;
    private final DixonNotificacionRepositorio dixonNotificaciones;
    private final DixonMatriculaRepositorio dixonMatriculas;
    private final DixonMensajeEnviadoRepositorio dixonMensajes;
    private final DixonConsultaAsesorRepositorio dixonConsultas;
    private final DixonCambioPendienteRepositorio dixonCambios;
    private final DixonUsoCarneRepositorio dixonUsos;
    private final DixonAjustesRepositorio dixonAjustes;

    private long dixonDesfaseDias;

    @Transactional
    public boolean dixonSembrar() {
        if (dixonAjustes.existsById(1L)) {
            return false;
        }
        dixonCargar(false);
        return true;
    }

    @Transactional
    public void dixonRestablecer() {
        dixonMensajes.deleteAllInBatch();
        dixonConsultas.deleteAll();
        dixonMatriculas.deleteAllInBatch();
        dixonExpedientes.deleteAll();
        dixonReservas.deleteAllInBatch();
        dixonPrestamos.deleteAllInBatch();
        dixonActivos.deleteAllInBatch();
        dixonFavoritos.deleteAllInBatch();
        dixonDocumentosPersonales.deleteAllInBatch();
        dixonNotificaciones.deleteAllInBatch();
        dixonCambios.deleteAllInBatch();
        dixonUsos.deleteAllInBatch();
        dixonPagos.deleteAll();
        dixonExpedientes.flush();
        dixonPagos.flush();
        dixonEquipos.findAll().forEach(dixonEquipo -> dixonEquipo.setDixonSaldo(dixonEquipo.getDixonUnidadesIniciales()));
        dixonCargar(true);
    }

    private void dixonCargar(boolean dixonConservar) {
        JsonNode dixonNodo = dixonJson.andraDemo();
        LocalDate dixonReferencia = LocalDate.parse(dixonNodo.path("fechaReferencia").asText());
        dixonDesfaseDias = ChronoUnit.DAYS.between(dixonReferencia, DixonFechas.dixonHoy());
        DixonPersona dixonEstudianteActual = dixonPersonas.findFirstByDixonPrincipalTrue().orElseThrow();

        for (JsonNode dixonNodo2 : dixonNodo.path("pagos")) {
            List<DixonElementoPago> dixonElementos = new ArrayList<>();
            for (JsonNode dixonNodo3 : dixonNodo2.path("items")) {
                dixonElementos.add(new DixonElementoPago(dixonNodo3.get(0).asText(), dixonNodo3.get(1).asInt(), new BigDecimal(dixonNodo3.get(2).asText())));
            }
            dixonPagos.save(DixonPago.builder().dixonCodigo(dixonNodo2.path("id").asText()).dixonPersona(dixonPersona(dixonNodo2.path("user").asText()))
                    .dixonTipo(DixonTipoPago.dixonDesdeClave(dixonNodo2.path("tipo").asText()))
                    .dixonTramite(dixonTramite(dixonNodo2.path("tupa").asText(null))).dixonConcepto(dixonNodo2.path("concepto").asText())
                    .dixonEstado(DixonEstadoPago.valueOf(dixonNodo2.path("estado").asText().toUpperCase()))
                    .dixonCreado(dixonMomento(dixonNodo2.path("creado").asText(null))).dixonVence(dixonFecha(dixonNodo2.path("vence").asText(null)))
                    .dixonPagado(dixonMomento(dixonNodo2.path("pagado").asText(null)))
                    .dixonMetodo(DixonMetodoPago.dixonDesdeClave(dixonNodo2.path("metodo").asText(null)))
                    .dixonOperacion(dixonNodo2.path("op").asText(null)).dixonBoleta(dixonNodo2.path("boleta").asText(null))
                    .dixonSede(dixonSede(dixonNodo2.path("sede").asText("central"))).dixonElementos(dixonElementos).build());
        }

        List<JsonNode> dixonOtrosExp = dixonLista(dixonNodo.path("others").path("sols"));
        List<JsonNode> dixonPropiosExp = dixonLista(dixonNodo.path("sols"));
        for (int dixonIndice = dixonOtrosExp.size() - 1; dixonIndice >= 0; dixonIndice--) {
            dixonGuardarExpediente(dixonOtrosExp.get(dixonIndice), null, true);
        }
        for (int dixonIndice = dixonPropiosExp.size() - 1; dixonIndice >= 0; dixonIndice--) {
            dixonGuardarExpediente(dixonPropiosExp.get(dixonIndice), dixonEstudianteActual, false);
        }

        List<JsonNode> dixonOtrasRes = dixonLista(dixonNodo.path("others").path("reservas"));
        List<JsonNode> dixonPropiasRes = dixonLista(dixonNodo.path("reservas"));
        for (int dixonIndice = dixonOtrasRes.size() - 1; dixonIndice >= 0; dixonIndice--) {
            dixonGuardarReserva(dixonOtrasRes.get(dixonIndice), null);
        }
        for (int dixonIndice = dixonPropiasRes.size() - 1; dixonIndice >= 0; dixonIndice--) {
            dixonGuardarReserva(dixonPropiasRes.get(dixonIndice), dixonEstudianteActual);
        }

        List<JsonNode> dixonOtrosHist = dixonLista(dixonNodo.path("others").path("hist"));
        List<JsonNode> dixonPropiosHist = dixonLista(dixonNodo.path("hist"));
        for (int dixonIndice = dixonOtrosHist.size() - 1; dixonIndice >= 0; dixonIndice--) {
            dixonGuardarPrestamo(dixonOtrosHist.get(dixonIndice), null, true);
        }
        for (int dixonIndice = dixonPropiosHist.size() - 1; dixonIndice >= 0; dixonIndice--) {
            dixonGuardarPrestamo(dixonPropiosHist.get(dixonIndice), dixonEstudianteActual, false);
        }

        JsonNode dixonNodo4 = dixonNodo.path("loan");
        dixonActivos.save(DixonPrestamoActivo.builder().dixonCodigo(dixonNodo4.path("code").asText()).dixonPersona(dixonEstudianteActual)
                .dixonEquipo(dixonNodo4.path("item").asText()).dixonRetiro(dixonNodo4.path("retiro").asText())
                .dixonLimite(LocalDateTime.now(DixonFechas.dixonReloj()).plusHours(2).plusMinutes(15).withSecond(0).withNano(0))
                .dixonRenovado(dixonNodo4.path("renewed").asBoolean()).dixonIncidencia(dixonNodo4.path("incident").asText(null))
                .dixonLugar(dixonNodo4.path("place").asText()).dixonDevuelto(false).build());

        dixonFavoritos(dixonNodo.path("favDocs"), DixonTipoFavorito.DOCUMENTO);
        dixonFavoritos(dixonNodo.path("myFav"), DixonTipoFavorito.MI_DOCUMENTO);
        dixonFavoritos(dixonNodo.path("favEq"), DixonTipoFavorito.EQUIPO);
        dixonFavoritos(dixonNodo.path("favSpaces"), DixonTipoFavorito.ESPACIO);

        for (JsonNode dixonNodo5 : dixonNodo.path("myDocs")) {
            dixonDocumentosPersonales.save(DixonDocumentoPersonal.builder().dixonCodigo(dixonNodo5.path("id").asText())
                    .dixonNombre(dixonNodo5.path("name").asText()).build());
        }

        dixonNotificaciones(dixonNodo.path("notifs"), DixonRol.ESTUDIANTE);
        dixonNotificaciones(dixonNodo.path("adminNotifs"), DixonRol.ADMINISTRADOR);

        JsonNode dixonNodo6 = dixonNodo.path("matricula");
        dixonMatriculas.save(DixonMatricula.builder().dixonPeriodo(dixonNodo6.path("periodo").asText())
                .dixonEstado("confirmada".equals(dixonNodo6.path("estado").asText()) ? DixonEstadoMatricula.CONFIRMADA : DixonEstadoMatricula.PENDIENTE)
                .dixonPago(dixonPagos.findByDixonCodigo(dixonNodo6.path("pago").asText()).orElseThrow()).build());

        List<JsonNode> dixonListaUsos = dixonLista(dixonNodo.path("usosCarne"));
        for (int dixonIndice = dixonListaUsos.size() - 1; dixonIndice >= 0; dixonIndice--) {
            JsonNode dixonNodo7 = dixonListaUsos.get(dixonIndice);
            dixonUsos.save(DixonUsoCarne.builder().dixonLugar(dixonNodo7.path("lugar").asText())
                    .dixonFecha(DixonFechas.dixonHoy().minusDays(dixonNodo7.path("diasAtras").asInt()).atTime(LocalTime.parse(dixonNodo7.path("hora").asText())))
                    .dixonConcedido(dixonNodo7.path("ok").asBoolean()).build());
        }

        JsonNode dixonContexto = dixonNodo.path("ctx");
        JsonNode dixonPref = dixonNodo.path("preferenciasAviso");
        String dixonEscuela = dixonContexto.path("fac").asText("all");
        DixonAjustes dixonAjustes2 = dixonAjustes.findById(1L).orElseGet(() -> DixonAjustes.builder().dixonId(1L).dixonMaximizada(false).build());
        if (!dixonConservar || dixonAjustes2.getDixonIdioma() == null) {
            dixonAjustes2.setDixonIdioma(dixonNodo.path("idioma").asText("es"));
        }
        dixonAjustes2.setDixonSede(dixonContexto.path("sede").asText("central"));
        dixonAjustes2.setDixonEscuela(DixonAjustes.DIXON_TODAS.equals(dixonEscuela) ? null : dixonEscuela);
        dixonAjustes2.setDixonAvisoWhatsapp(dixonPref.path("whatsapp").asBoolean(true));
        dixonAjustes2.setDixonAvisoCorreo(dixonPref.path("correo").asBoolean(true));
        dixonAjustes2.setDixonAvisoAplicacion(dixonPref.path("app").asBoolean(true));
        if (!dixonConservar) {
            dixonAjustes2.setDixonSesion(null);
            dixonAjustes2.setDixonSesionDesde(null);
            dixonAjustes2.setDixonReducirMovimiento(false);
        }
        dixonAjustes2.setDixonVersion(dixonNodo.path("v").asInt(5));
        dixonAjustes.save(dixonAjustes2);
    }

    private void dixonGuardarExpediente(JsonNode dixonNodo, DixonPersona dixonPropietario, boolean dixonDesplazar) {
        DixonPersona dixonPersona = dixonPropietario != null ? dixonPropietario : dixonPersona(dixonNodo.path("user").asText());
        DixonExpediente dixonExpediente = DixonExpediente.builder().dixonCodigo(dixonNodo.path("id").asText()).dixonPersona(dixonPersona)
                .dixonSede(dixonNodo.has("sede") ? dixonSede(dixonNodo.path("sede").asText()) : dixonPersona.getDixonSede())
                .dixonClase(DixonClaseExpediente.valueOf(dixonNodo.path("kind").asText("doc").toUpperCase()))
                .dixonTramite(dixonTramite(dixonNodo.path("tupa").asText(null)))
                .dixonPago(dixonNodo.hasNonNull("pago") ? dixonPagos.findByDixonCodigo(dixonNodo.path("pago").asText()).orElse(null) : null)
                .dixonGrupo(DixonGrupoExpediente.dixonDesdeClave(dixonNodo.path("group").asText()))
                .dixonIcono(dixonNodo.path("icon").asText("doc")).dixonRotuloIcono(dixonNodo.path("iconLabel").asText(""))
                .dixonTono(dixonNodo.path("tone").asText("neutral")).dixonEstado(dixonNodo.path("status").asText())
                .dixonTitulo(dixonNodo.path("title").asText()).dixonDescripcion(dixonNodo.path("desc").asText(null))
                .dixonPaso(dixonNodo.path("step").asInt(1)).dixonObservacion(dixonNodo.path("obs").asText(null))
                .dixonDestacado(dixonNodo.path("flag").asBoolean(false)).dixonNota(dixonNodo.path("note").asText(null))
                .dixonFecha(dixonMomento(dixonNodo.path("fecha").asText(null))).build();
        for (JsonNode dixonNodo2 : dixonNodo.path("meta")) {
            String dixonValor = dixonNodo2.get(1).asText();
            dixonExpediente.getDixonDatos().add(new DixonDatoExpediente(dixonNodo2.get(0).asText(), dixonDesplazar ? dixonDesplazarTexto(dixonValor) : dixonValor));
        }
        dixonNodo.path("tags").forEach(dixonNodo3 -> dixonExpediente.getDixonEtiquetas().add(dixonNodo3.asText()));
        for (JsonNode dixonNodo4 : dixonNodo.path("acts")) {
            String dixonAccion = dixonNodo4.get(1).asText();
            String dixonTexto = "route-sol".equals(dixonAccion) ? "Seguimiento en vivo" : dixonNodo4.get(2).asText();
            dixonExpediente.getDixonAcciones().add(new DixonAccionExpediente(dixonNodo4.get(0).asText(), dixonAccion, dixonTexto));
        }
        JsonNode dixonSol = dixonNodo.path("solicitante");
        if (!dixonSol.isMissingNode()) {
            dixonExpediente.setDixonSolicitante(DixonDatosSolicitante.builder().dixonTipo(dixonSol.path("tipo").asText()).dixonNombre(dixonSol.path("nombre").asText())
                    .dixonDni(dixonSol.path("dni").asText()).dixonCodigo(dixonSol.path("codigo").asText())
                    .dixonEscuela(dixonSol.path("fac").asText(null)).dixonCorreo(dixonSol.path("correo").asText()).build());
        }
        dixonExpedientes.save(dixonExpediente);
    }

    private void dixonGuardarReserva(JsonNode dixonNodo, DixonPersona dixonPropietario) {
        DixonEspacio dixonEspacio = dixonEspacios.findFirstByDixonNombre(dixonNodo.path("space").asText()).orElseThrow();
        dixonReservas.save(DixonReserva.builder().dixonCodigo(dixonNodo.path("id").asText())
                .dixonPersona(dixonPropietario != null ? dixonPropietario : dixonPersona(dixonNodo.path("user").asText()))
                .dixonEspacio(dixonEspacio).dixonFecha(dixonFecha(dixonNodo.path("date").asText())).dixonHorario(dixonNodo.path("hora").asText())
                .dixonEstado(DixonEstadoReserva.dixonDesdeEtiqueta(dixonNodo.path("status").asText())).dixonMotivo(dixonNodo.path("motivo").asText())
                .dixonAsistentes(dixonNodo.path("asist").asInt()).dixonNota(dixonNodo.path("nota").asText(null)).build());
    }

    private void dixonGuardarPrestamo(JsonNode dixonAlto, DixonPersona dixonPropietario, boolean dixonDesplazar) {
        String dixonDetalle = dixonDesplazar ? dixonDesplazarTexto(dixonAlto.path("sub").asText()) : dixonAlto.path("sub").asText();
        String dixonSede = dixonDetalle.contains("Sede Norte") ? "norte" : dixonDetalle.contains("Sede Sur") ? "sur" : "central";
        DixonEquipo dixonEquipo = dixonEquipos.findAll().stream().filter(dixonPosicionX -> dixonAlto.path("name").asText().startsWith(dixonPosicionX.getDixonNombre()))
                .findFirst().orElse(null);
        dixonPrestamos.save(DixonPrestamo.builder().dixonCodigo(dixonAlto.path("code").asText()).dixonClave(dixonAlto.path("id").asText())
                .dixonPersona(dixonPropietario != null ? dixonPropietario : dixonPersona(dixonAlto.path("user").asText()))
                .dixonEquipo(dixonEquipo).dixonNombre(dixonAlto.path("name").asText()).dixonDetalle(dixonDetalle)
                .dixonEstado(DixonEstadoPrestamo.dixonDesdeEtiqueta(dixonAlto.path("status").asText())).dixonSede(dixonSede(dixonSede)).build());
    }

    private void dixonFavoritos(JsonNode dixonCodigos, DixonTipoFavorito dixonTipo) {
        dixonCodigos.forEach(dixonNodo -> dixonFavoritos.save(DixonFavorito.builder().dixonTipo(dixonTipo).dixonCodigo(dixonNodo.asText()).build()));
    }

    private void dixonNotificaciones(JsonNode dixonLista, DixonRol dixonRol) {
        List<JsonNode> dixonLista2 = dixonLista(dixonLista);
        LocalDateTime dixonBase = DixonFechas.dixonAhora().minusHours(dixonLista2.size());
        for (int dixonIndice = dixonLista2.size() - 1; dixonIndice >= 0; dixonIndice--) {
            JsonNode dixonNodo = dixonLista2.get(dixonIndice);
            dixonNotificaciones.save(DixonNotificacion.builder().dixonDestinatario(dixonRol).dixonTexto(dixonNodo.path("text").asText())
                    .dixonRuta(dixonNodo.path("to").asText()).dixonLeida(dixonNodo.path("read").asBoolean())
                    .dixonCreada(dixonBase.plusHours(dixonLista2.size() - dixonIndice)).build());
        }
    }

    private List<JsonNode> dixonLista(JsonNode dixonNodo) {
        List<JsonNode> dixonLista = new ArrayList<>();
        dixonNodo.forEach(dixonLista::add);
        return dixonLista;
    }

    private DixonPersona dixonPersona(String dixonCodigo) {
        return dixonPersonas.findByDixonCodigo(dixonCodigo).orElseThrow(() -> new IllegalStateException("Persona " + dixonCodigo));
    }

    private DixonSede dixonSede(String dixonCodigo) {
        return dixonSedes.findByDixonCodigo(dixonCodigo).orElseThrow();
    }

    private DixonTramite dixonTramite(String dixonCodigo) {
        return dixonCodigo == null || dixonCodigo.isBlank() ? null : dixonTramites.findByDixonCodigo(dixonCodigo).orElse(null);
    }

    private LocalDate dixonFecha(String dixonTexto) {
        LocalDate dixonFecha = DixonFechas.dixonLeerFecha(dixonTexto);
        return dixonFecha == null ? null : dixonFecha.plusDays(dixonDesfaseDias);
    }

    private LocalDateTime dixonMomento(String dixonTexto) {
        LocalDateTime dixonFechaHora = DixonFechas.dixonLeerMomento(dixonTexto);
        return dixonFechaHora == null ? null : dixonFechaHora.plusDays(dixonDesfaseDias);
    }

    private String dixonDesplazarTexto(String dixonTexto) {
        if (dixonTexto == null || dixonDesfaseDias == 0) {
            return dixonTexto;
        }
        Matcher dixonCoincidencia = DIXON_FECHA_DMY.matcher(dixonTexto);
        StringBuilder dixonTexto2 = new StringBuilder();
        while (dixonCoincidencia.find()) {
            LocalDate dixonFecha = LocalDate.of(Integer.parseInt(dixonCoincidencia.group(3)), Integer.parseInt(dixonCoincidencia.group(2)),
                    Integer.parseInt(dixonCoincidencia.group(1))).plusDays(dixonDesfaseDias);
            dixonCoincidencia.appendReplacement(dixonTexto2, DixonFechas.dixonDmy(dixonFecha));
        }
        dixonCoincidencia.appendTail(dixonTexto2);
        return dixonTexto2.toString();
    }
}
