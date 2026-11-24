package pe.andra.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pe.andra.BaseTemporal;
import pe.andra.exception.AndraNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.*;
import pe.andra.util.AndraFechas;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class IntegracionTest {

    @DynamicPropertySource
    static void base(DynamicPropertyRegistry registro) throws Exception {
        BaseTemporal.configurar(registro, "andra-integracion");
    }

    @Autowired private AndraDemoServicio demo;
    @Autowired private AndraPagoServicio pagos;
    @Autowired private AndraCatalogoServicio catalogo;
    @Autowired private AndraSesionServicio sesion;
    @Autowired private AndraExpedienteServicio expedientes;
    @Autowired private AndraAdministradorServicio admin;
    @Autowired private AndraNotificacionServicio notificaciones;
    @Autowired private AndraConexionServicio conexion;
    @Autowired private AndraReservaServicio reservas;
    @Autowired private AndraOcupacionServicio ocupacion;
    @Autowired private AndraCarneServicio carne;
    @Autowired private AndraSeguimientoServicio seguimiento;
    @Autowired private AndraAnaliticaServicio analitica;
    @Autowired private AndraContextoServicio contexto;
    @Autowired private AndraFavoritoServicio favoritos;
    @Autowired private AndraReservaRepositorio reservaRepository;
    @Autowired private AndraMensajeEnviadoRepositorio mensajes;
    @Autowired private AndraAjustesServicio ajustes;
    @Autowired private AndraConsultaAsesorServicio consultas;

    @BeforeEach
    void preparar() {
        demo.andraRestablecer();
        conexion.andraSimularSinConexion(false);
        conexion.andraRedPropiedad().set(true);
    }

    @AfterEach
    void limpiar() {
        conexion.andraSimularSinConexion(false);
    }

    private AndraDatosSolicitante yo() {
        AndraPersona p = sesion.andraEstudiante();
        return AndraDatosSolicitante.builder().andraTipo("Estudiante").andraNombre(p.getAndraNombre()).andraDni(p.getAndraDni()).andraCodigo(p.getAndraCodigo())
                .andraEscuela("isi").andraCorreo(p.getAndraCorreo()).build();
    }

    @Test
    void consultaConAsesorLimitadaYAmpliable() {
        AndraExpediente e = expedientes.andraMios().stream().filter(AndraConsultaAsesorServicio::andraDisponible).findFirst().orElseThrow();
        AndraConsultaAsesor c = consultas.andraAbrir(e.getAndraCodigo());
        assertThat(c.getAndraMensajes().get(0).getAndraTexto()).contains(e.getAndraCodigo()).contains(e.getAndraTitulo());
        assertThat(c.andraRestantes()).isEqualTo(3);
        assertThatThrownBy(() -> consultas.andraSolicitarAmpliacion(e.getAndraCodigo())).isInstanceOf(AndraNegocioExcepcion.class);
        for (int i = 0; i < 3; i++) {
            consultas.andraPreguntar(e.getAndraCodigo(), "¿Cuándo estará listo?");
        }
        assertThat(consultas.andraAbrir(e.getAndraCodigo()).andraRestantes()).isZero();
        assertThatThrownBy(() -> consultas.andraPreguntar(e.getAndraCodigo(), "Otra")).isInstanceOf(AndraNegocioExcepcion.class);
        assertThat(consultas.andraSolicitarAmpliacion(e.getAndraCodigo()).getAndraAmpliacion()).isEqualTo(AndraEstadoAmpliacion.PENDIENTE);
        AndraConsultaAsesor ampliada = consultas.andraEvaluarAmpliacion(e.getAndraCodigo());
        assertThat(ampliada.getAndraAmpliacion()).isEqualTo(AndraEstadoAmpliacion.APROBADA);
        assertThat(ampliada.andraRestantes()).isEqualTo(3);
        assertThatThrownBy(() -> consultas.andraSolicitarAmpliacion(e.getAndraCodigo())).isInstanceOf(AndraNegocioExcepcion.class);
        AndraExpediente listo = expedientes.andraMios().stream().filter(x -> !AndraConsultaAsesorServicio.andraDisponible(x)).findFirst().orElseThrow();
        assertThatThrownBy(() -> consultas.andraAbrir(listo.getAndraCodigo())).isInstanceOf(AndraNegocioExcepcion.class);
    }

    @Test
    void ingresoYPermisosPorRol() {
        assertThatThrownBy(() -> sesion.andraIngresar(AndraRol.ESTUDIANTE, "20240157", "mal")).isInstanceOf(AndraNegocioExcepcion.class);
        assertThat(sesion.andraIngresar(AndraRol.ESTUDIANTE, "72458913", "andra2026")).isEqualTo(AndraRol.ESTUDIANTE);
        assertThat(AndraSesionServicio.andraPuedeAcceder(sesion.andraRol(), "admin")).isFalse();
        sesion.andraCerrar();
        assertThat(sesion.andraIngresar(AndraRol.ADMINISTRADOR, "RMARTINEZ@institucion.edu", "admin2026")).isEqualTo(AndraRol.ADMINISTRADOR);
        assertThat(sesion.andraEsAdministrador()).isTrue();
        sesion.andraCerrar();
        assertThat(sesion.andraConectado()).isFalse();
    }

    @Test
    void efectosDelPagoEnOrdenExpedienteYMatricula() {
        AndraTramite t = catalogo.andraTramite("t-cert").orElseThrow();
        AndraPago p = pagos.andraCrearOrden(t, yo(), 1, "Trabajo", "central", null);
        AndraExpediente e = expedientes.andraDePago(p).orElseThrow();
        assertThat(e.getAndraEstado()).isEqualTo("Pago pendiente en Andra Pay");
        long antes = notificaciones.andraNoLeidas(AndraRol.ADMINISTRADOR);
        AndraPago pagado = pagos.andraCompletar(p.getAndraCodigo(), AndraMetodoPago.YAPE);
        assertThat(pagado.andraEsPagado()).isTrue();
        assertThat(pagado.getAndraBoleta()).startsWith("B001-");
        assertThat(pagado.getAndraOperacion()).startsWith("YP");
        assertThat(pagado.getAndraRecojoFecha()).isNotNull();
        AndraExpediente actualizado = expedientes.andraObtener(e.getAndraCodigo());
        assertThat(actualizado.getAndraEstado()).isEqualTo("Pagado · En emisión");
        assertThat(actualizado.getAndraAcciones()).extracting(AndraAccionExpediente::getAndraAccion).contains("gen-sol", "rcpt-sol", "route-sol");
        assertThat(notificaciones.andraNoLeidas(AndraRol.ADMINISTRADOR)).isEqualTo(antes + 1);
        AndraMatricula m = pagos.andraMatricula();
        assertThat(m.andraEsConfirmada()).isFalse();
        pagos.andraCompletar(m.getAndraPago().getAndraCodigo(), AndraMetodoPago.VISA);
        assertThat(pagos.andraMatricula().andraEsConfirmada()).isTrue();
        assertThat(carne.andraVigente()).isTrue();
        assertThat(notificaciones.andraListar(AndraRol.ESTUDIANTE).get(0).getAndraTexto()).contains("quedó confirmada");
    }

    @Test
    void desistirAnulaLaOrdenPendiente() {
        AndraPago p = pagos.andraCrearOrden(catalogo.andraTramite("t-cest").orElseThrow(), yo(), 1, "Trámite personal", "central", null);
        AndraExpediente e = expedientes.andraDePago(p).orElseThrow();
        expedientes.andraDesistir(e.getAndraCodigo());
        assertThat(pagos.andraObtener(p.getAndraCodigo()).getAndraEstado()).isEqualTo(AndraEstadoPago.ANULADO);
    }

    @Test
    void validacionDelSolicitante() {
        AndraTramite egresado = catalogo.andraTramite("t-cegr").orElseThrow();
        assertThat(pagos.andraValidarSolicitante(egresado, yo(), false)).containsEntry("publico", "Este trámite es para: Egresado.");
        AndraTramite carneT = catalogo.andraTramite("t-carne").orElseThrow();
        assertThat(pagos.andraValidarSolicitante(carneT, yo(), false)).containsEntry("foto", "Sube tu foto para el carné.");
        AndraDatosSolicitante malo = yo().toBuilder().andraDni("123").andraCorreo("x").build();
        assertThat(pagos.andraValidarSolicitante(carneT, malo, true)).containsKeys("dni", "correo");
    }

    @Test
    void accionesDelAdministradorNotificanYEnvianMensajes() {
        AndraPago p = pagos.andraCrearOrden(catalogo.andraTramite("t-cert").orElseThrow(), yo(), 1, "Trabajo", "central", null);
        AndraExpediente e = expedientes.andraDePago(p).orElseThrow();
        assertThatThrownBy(() -> admin.andraEmitir(e.getAndraCodigo())).hasMessage("Esperando pago");
        pagos.andraCompletar(p.getAndraCodigo(), AndraMetodoPago.PLIN);
        int mensajesAntes = mensajes.findByAndraExpedienteOrderByAndraIdDesc(expedientes.andraObtener(e.getAndraCodigo())).size();
        assertThat(mensajesAntes).isEqualTo(2);
        long antes = notificaciones.andraNoLeidas(AndraRol.ESTUDIANTE);
        admin.andraEmitir(e.getAndraCodigo());
        assertThat(expedientes.andraObtener(e.getAndraCodigo()).getAndraGrupo()).isEqualTo(AndraGrupoExpediente.LISTO);
        assertThat(notificaciones.andraNoLeidas(AndraRol.ESTUDIANTE)).isEqualTo(antes + 1);
        seguimiento.andraCambiarPreferencia("wa", false);
        admin.andraEntregar(e.getAndraCodigo());
        List<AndraMensajeEnviado> lista = mensajes.findByAndraExpedienteOrderByAndraIdDesc(expedientes.andraObtener(e.getAndraCodigo()));
        assertThat(lista).hasSize(5);
        assertThat(lista.get(0).getAndraCanal()).isEqualTo(AndraCanalAviso.CORREO);
        assertThat(AndraSeguimientoServicio.andraCalcular(expedientes.andraObtener(e.getAndraCodigo())).andraEstimado()).isEqualTo("Entregado");
        AndraExpediente otro = expedientes.andraMios().stream().filter(x -> x.getAndraGrupo() == AndraGrupoExpediente.TRAMITE && x.getAndraPago() == null).findFirst().orElseThrow();
        assertThatThrownBy(() -> admin.andraObservar(otro.getAndraCodigo(), "corto")).hasMessage("Escribe al menos 15 caracteres.");
        admin.andraObservar(otro.getAndraCodigo(), "Falta adjuntar la copia del DNI legible.");
        assertThat(expedientes.andraObtener(otro.getAndraCodigo()).getAndraGrupo()).isEqualTo(AndraGrupoExpediente.OBSERVADO);
        assertThat(notificaciones.andraListar(AndraRol.ESTUDIANTE).get(0).getAndraTexto()).contains("fue observado");
        AndraReserva r = reservas.andraMias().stream().filter(x -> x.getAndraEstado() == AndraEstadoReserva.PENDIENTE).findFirst().orElseThrow();
        admin.andraRechazarReserva(r.getAndraId(), "El auditorio está en mantenimiento.");
        assertThat(reservaRepository.findById(r.getAndraId()).orElseThrow().getAndraEstado()).isEqualTo(AndraEstadoReserva.RECHAZADO);
        AndraPago pagado = pagos.andraMios().stream().filter(AndraPago::andraEsPagado).findFirst().orElseThrow();
        admin.andraReembolsar(pagado.getAndraCodigo());
        assertThat(pagos.andraObtener(pagado.getAndraCodigo()).getAndraEstado()).isEqualTo(AndraEstadoPago.REEMBOLSADO);
    }

    @Test
    void sinConexionLaColaCreceLosPagosSeBloqueanYSeVacia() {
        conexion.andraSimularSinConexion(true);
        assertThat(conexion.andraSinConexion()).isTrue();
        favoritos.andraAlternar(AndraTipoFavorito.EQUIPO, "e1");
        expedientes.andraRegistrar("Traslado interno", "EP Ingeniería de Sistemas", "central", "Solicito traslado interno por motivos personales.", null);
        assertThat(conexion.andraContarPendientes()).isEqualTo(2);
        String m = pagos.andraMatricula().getAndraPago().getAndraCodigo();
        assertThatThrownBy(() -> pagos.andraCompletar(m, AndraMetodoPago.YAPE)).hasMessage("Necesitas conexión para pagar");
        conexion.andraSimularSinConexion(false);
        assertThat(conexion.andraSincronizar()).isEqualTo(2);
        assertThat(conexion.andraContarPendientes()).isZero();
        favoritos.andraAlternar(AndraTipoFavorito.EQUIPO, "e2");
        assertThat(conexion.andraContarPendientes()).isZero();
    }

    @Test
    void persistenciaDeIdaYVuelta() {
        AndraEspacio redes = catalogo.andraEspacio("lab-redes").orElseThrow();
        LocalDate manana = AndraFechas.andraHoy().plusDays(1);
        AndraReserva r = reservas.andraReservar(redes, manana, "08:00 - 10:00", 20, "Práctica calificada de Redes");
        AndraReserva leida = reservaRepository.findById(r.getAndraId()).orElseThrow();
        assertThat(leida.getAndraEspacio().getAndraCodigo()).isEqualTo("lab-redes");
        assertThat(leida.getAndraFecha()).isEqualTo(manana);
        assertThat(leida.getAndraEstado()).isEqualTo(AndraEstadoReserva.PENDIENTE);
        assertThatThrownBy(() -> reservas.andraReservar(redes, manana, "08:00 - 10:00", 5, "Otra clase del curso")).hasMessage("Ese horario ya está reservado. Elige otro.");
        ajustes.andraCambiar(a -> a.setAndraIdioma("qu"));
        assertThat(ajustes.andraObtener().getAndraIdioma()).isEqualTo("qu");
        assertThat(contexto.andraCambiarEscuela("iel")).isEqualTo("EP Ingeniería Eléctrica · Sede Norte.");
        assertThat(contexto.andraCodigoSede()).isEqualTo("norte");
        assertThat(contexto.andraCambiarSede("central")).isEqualTo("Mostrando información de la Sede Central · todas las escuelas.");
        assertThat(contexto.andraEscuela()).isNull();
        demo.andraRestablecer();
        assertThat(reservaRepository.findById(r.getAndraId())).isEmpty();
        assertThat(ajustes.andraObtener().getAndraIdioma()).isEqualTo("qu");
    }

    @Test
    void mapaLibrePorTerminarYOcupado() {
        AndraEspacio cnc = catalogo.andraEspacio("taller-cnc").orElseThrow();
        assertThat(ocupacion.andraEstado(cnc, 0, 0)).isEqualTo(AndraOcupacionServicio.AndraEstadoOcupacion.OCUPADO);
        AndraEspacio e = catalogo.andraEspacios().stream().filter(x -> !x.isAndraFueraDeServicio()).filter(x -> AndraReservaServicio.andraDisponibleSimulado(x.getAndraNombre(), 3, "08:00")
                && AndraReservaServicio.andraDisponibleSimulado(x.getAndraNombre(), 3, "10:00")).findFirst().orElseThrow();
        assertThat(ocupacion.andraEstado(e, 0, 3)).isEqualTo(AndraOcupacionServicio.AndraEstadoOcupacion.LIBRE);
        reservas.andraReservar(e, AndraFechas.andraHoy().plusDays(3), "10:00 - 12:00", 1, "Prueba del mapa en vivo");
        assertThat(ocupacion.andraEstado(e, 0, 3)).isEqualTo(AndraOcupacionServicio.AndraEstadoOcupacion.POR_TERMINAR);
        assertThat(ocupacion.andraEstado(e, 1, 3)).isEqualTo(AndraOcupacionServicio.AndraEstadoOcupacion.OCUPADO);
    }

    @Test
    void carneAceptaFranjaActualYVecinasYRechazaVencidoOAlterado() {
        long f = AndraCarneServicio.andraFranja(AndraFechas.andraMilis());
        String token = AndraCarneServicio.andraToken("20240157", f);
        assertThat(carne.andraVerificar(token, f).andraCorrecto()).isTrue();
        assertThat(carne.andraVerificar(token, f + 1).andraCorrecto()).isTrue();
        assertThat(carne.andraVerificar(token, f - 1).andraCorrecto()).isTrue();
        assertThat(carne.andraVerificar(token, f + 2).andraMensaje()).isEqualTo("Código vencido. Pide al estudiante que muestre el código actual.");
        String alterado = "ANDRA-ID|20240157|" + f + "|ZZZZZZ";
        assertThat(carne.andraVerificar(alterado, f).andraMensaje()).isEqualTo("Firma inválida: el código fue alterado.");
        assertThat(carne.andraVerificar("72458913", f).andraMensaje()).isEqualTo("Estudiante encontrado (validación manual por código)");
        assertThat(carne.andraVerificar("000", f).andraCorrecto()).isFalse();
        assertThat(carne.andraSimularLectura().isAndraConcedido()).isFalse();
    }

    @Test
    void analisisPorPeriodo() {
        contexto.andraCambiarSede(AndraAjustes.ANDRA_TODAS);
        AndraAnaliticaServicio.AndraAnalisis sem = analitica.andraAnalizar("sem");
        AndraAnaliticaServicio.AndraAnalisis siete = analitica.andraAnalizar("7");
        assertThat(sem.andraTramitesYOrdenes()).isGreaterThanOrEqualTo(siete.andraTramitesYOrdenes());
        assertThat(sem.andraRecaudacion()).isGreaterThanOrEqualTo(siete.andraRecaudacion());
        assertThat(sem.andraSuperior()).hasSizeLessThanOrEqualTo(6);
        for (int i = 1; i < sem.andraSuperior().size(); i++) {
            assertThat(sem.andraSuperior().get(i - 1).andraValor()).isGreaterThanOrEqualTo(sem.andraSuperior().get(i).andraValor());
        }
        assertThat(sem.andraHallazgos()).hasSize(4);
        assertThat(sem.andraCalor()[sem.andraPico()[0]][sem.andraPico()[1]]).isEqualTo(sem.andraCalorMaximo());
        assertThat(sem.andraSubutilizados()).hasSize(4);
        assertThat(sem.andraDemandados()).hasSize(3);
        assertThat(sem.andraPorSede()).hasSize(3);
    }
}
