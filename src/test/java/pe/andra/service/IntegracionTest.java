package pe.andra.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pe.andra.BaseTemporal;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.*;
import pe.andra.repository.*;
import pe.andra.util.DixonFechas;

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

    @Autowired private DixonDemoServicio demo;
    @Autowired private DixonPagoServicio pagos;
    @Autowired private DixonCatalogoServicio catalogo;
    @Autowired private DixonSesionServicio sesion;
    @Autowired private DixonExpedienteServicio expedientes;
    @Autowired private DixonAdministradorServicio admin;
    @Autowired private DixonNotificacionServicio notificaciones;
    @Autowired private DixonConexionServicio conexion;
    @Autowired private DixonReservaServicio reservas;
    @Autowired private DixonOcupacionServicio ocupacion;
    @Autowired private DixonCarneServicio carne;
    @Autowired private DixonSeguimientoServicio seguimiento;
    @Autowired private DixonAnaliticaServicio analitica;
    @Autowired private DixonContextoServicio contexto;
    @Autowired private DixonFavoritoServicio favoritos;
    @Autowired private DixonReservaRepositorio reservaRepository;
    @Autowired private DixonMensajeEnviadoRepositorio mensajes;
    @Autowired private DixonAjustesServicio ajustes;
    @Autowired private DixonConsultaAsesorServicio consultas;

    @BeforeEach
    void preparar() {
        demo.dixonRestablecer();
        conexion.dixonSimularSinConexion(false);
        conexion.dixonRedPropiedad().set(true);
    }

    @AfterEach
    void limpiar() {
        conexion.dixonSimularSinConexion(false);
    }

    private DixonDatosSolicitante yo() {
        DixonPersona p = sesion.dixonEstudiante();
        return DixonDatosSolicitante.builder().dixonTipo("Estudiante").dixonNombre(p.getDixonNombre()).dixonDni(p.getDixonDni()).dixonCodigo(p.getDixonCodigo())
                .dixonEscuela("isi").dixonCorreo(p.getDixonCorreo()).build();
    }

    @Test
    void consultaConAsesorLimitadaYAmpliable() {
        DixonExpediente e = expedientes.dixonMios().stream().filter(DixonConsultaAsesorServicio::dixonDisponible).findFirst().orElseThrow();
        DixonConsultaAsesor c = consultas.dixonAbrir(e.getDixonCodigo());
        assertThat(c.getDixonMensajes().get(0).getDixonTexto()).contains(e.getDixonCodigo()).contains(e.getDixonTitulo());
        assertThat(c.dixonRestantes()).isEqualTo(3);
        assertThatThrownBy(() -> consultas.dixonSolicitarAmpliacion(e.getDixonCodigo())).isInstanceOf(DixonNegocioExcepcion.class);
        for (int i = 0; i < 3; i++) {
            consultas.dixonPreguntar(e.getDixonCodigo(), "¿Cuándo estará listo?");
        }
        assertThat(consultas.dixonAbrir(e.getDixonCodigo()).dixonRestantes()).isZero();
        assertThatThrownBy(() -> consultas.dixonPreguntar(e.getDixonCodigo(), "Otra")).isInstanceOf(DixonNegocioExcepcion.class);
        assertThat(consultas.dixonSolicitarAmpliacion(e.getDixonCodigo()).getDixonAmpliacion()).isEqualTo(DixonEstadoAmpliacion.PENDIENTE);
        DixonConsultaAsesor ampliada = consultas.dixonEvaluarAmpliacion(e.getDixonCodigo());
        assertThat(ampliada.getDixonAmpliacion()).isEqualTo(DixonEstadoAmpliacion.APROBADA);
        assertThat(ampliada.dixonRestantes()).isEqualTo(3);
        assertThatThrownBy(() -> consultas.dixonSolicitarAmpliacion(e.getDixonCodigo())).isInstanceOf(DixonNegocioExcepcion.class);
        DixonExpediente listo = expedientes.dixonMios().stream().filter(x -> !DixonConsultaAsesorServicio.dixonDisponible(x)).findFirst().orElseThrow();
        assertThatThrownBy(() -> consultas.dixonAbrir(listo.getDixonCodigo())).isInstanceOf(DixonNegocioExcepcion.class);
    }

    @Test
    void ingresoYPermisosPorRol() {
        assertThatThrownBy(() -> sesion.dixonIngresar(DixonRol.ESTUDIANTE, "20240157", "mal")).isInstanceOf(DixonNegocioExcepcion.class);
        assertThat(sesion.dixonIngresar(DixonRol.ESTUDIANTE, "72458913", "andra2026")).isEqualTo(DixonRol.ESTUDIANTE);
        assertThat(DixonSesionServicio.dixonPuedeAcceder(sesion.dixonRol(), "admin")).isFalse();
        sesion.dixonCerrar();
        assertThat(sesion.dixonIngresar(DixonRol.ADMINISTRADOR, "RMARTINEZ@institucion.edu", "admin2026")).isEqualTo(DixonRol.ADMINISTRADOR);
        assertThat(sesion.dixonEsAdministrador()).isTrue();
        sesion.dixonCerrar();
        assertThat(sesion.dixonConectado()).isFalse();
    }

    @Test
    void efectosDelPagoEnOrdenExpedienteYMatricula() {
        DixonTramite t = catalogo.dixonTramite("t-cert").orElseThrow();
        DixonPago p = pagos.dixonCrearOrden(t, yo(), 1, "Trabajo", "central", null);
        DixonExpediente e = expedientes.dixonDePago(p).orElseThrow();
        assertThat(e.getDixonEstado()).isEqualTo("Pago pendiente en Andra Pay");
        long antes = notificaciones.dixonNoLeidas(DixonRol.ADMINISTRADOR);
        DixonPago pagado = pagos.dixonCompletar(p.getDixonCodigo(), DixonMetodoPago.YAPE);
        assertThat(pagado.dixonEsPagado()).isTrue();
        assertThat(pagado.getDixonBoleta()).startsWith("B001-");
        assertThat(pagado.getDixonOperacion()).startsWith("YP");
        assertThat(pagado.getDixonRecojoFecha()).isNotNull();
        DixonExpediente actualizado = expedientes.dixonObtener(e.getDixonCodigo());
        assertThat(actualizado.getDixonEstado()).isEqualTo("Pagado · En emisión");
        assertThat(actualizado.getDixonAcciones()).extracting(DixonAccionExpediente::getDixonAccion).contains("gen-sol", "rcpt-sol", "route-sol");
        assertThat(notificaciones.dixonNoLeidas(DixonRol.ADMINISTRADOR)).isEqualTo(antes + 1);
        DixonMatricula m = pagos.dixonMatricula();
        assertThat(m.dixonEsConfirmada()).isFalse();
        pagos.dixonCompletar(m.getDixonPago().getDixonCodigo(), DixonMetodoPago.VISA);
        assertThat(pagos.dixonMatricula().dixonEsConfirmada()).isTrue();
        assertThat(carne.dixonVigente()).isTrue();
        assertThat(notificaciones.dixonListar(DixonRol.ESTUDIANTE).get(0).getDixonTexto()).contains("quedó confirmada");
    }

    @Test
    void desistirAnulaLaOrdenPendiente() {
        DixonPago p = pagos.dixonCrearOrden(catalogo.dixonTramite("t-cest").orElseThrow(), yo(), 1, "Trámite personal", "central", null);
        DixonExpediente e = expedientes.dixonDePago(p).orElseThrow();
        expedientes.dixonDesistir(e.getDixonCodigo());
        assertThat(pagos.dixonObtener(p.getDixonCodigo()).getDixonEstado()).isEqualTo(DixonEstadoPago.ANULADO);
    }

    @Test
    void validacionDelSolicitante() {
        DixonTramite egresado = catalogo.dixonTramite("t-cegr").orElseThrow();
        assertThat(pagos.dixonValidarSolicitante(egresado, yo(), false)).containsEntry("publico", "Este trámite es para: Egresado.");
        DixonTramite carneT = catalogo.dixonTramite("t-carne").orElseThrow();
        assertThat(pagos.dixonValidarSolicitante(carneT, yo(), false)).containsEntry("foto", "Sube tu foto para el carné.");
        DixonDatosSolicitante malo = yo().toBuilder().dixonDni("123").dixonCorreo("x").build();
        assertThat(pagos.dixonValidarSolicitante(carneT, malo, true)).containsKeys("dni", "correo");
    }

    @Test
    void accionesDelAdministradorNotificanYEnvianMensajes() {
        DixonPago p = pagos.dixonCrearOrden(catalogo.dixonTramite("t-cert").orElseThrow(), yo(), 1, "Trabajo", "central", null);
        DixonExpediente e = expedientes.dixonDePago(p).orElseThrow();
        assertThatThrownBy(() -> admin.dixonEmitir(e.getDixonCodigo())).hasMessage("Esperando pago");
        pagos.dixonCompletar(p.getDixonCodigo(), DixonMetodoPago.PLIN);
        int mensajesAntes = mensajes.findByDixonExpedienteOrderByDixonIdDesc(expedientes.dixonObtener(e.getDixonCodigo())).size();
        assertThat(mensajesAntes).isEqualTo(2);
        long antes = notificaciones.dixonNoLeidas(DixonRol.ESTUDIANTE);
        admin.dixonEmitir(e.getDixonCodigo());
        assertThat(expedientes.dixonObtener(e.getDixonCodigo()).getDixonGrupo()).isEqualTo(DixonGrupoExpediente.LISTO);
        assertThat(notificaciones.dixonNoLeidas(DixonRol.ESTUDIANTE)).isEqualTo(antes + 1);
        seguimiento.dixonCambiarPreferencia("wa", false);
        admin.dixonEntregar(e.getDixonCodigo());
        List<DixonMensajeEnviado> lista = mensajes.findByDixonExpedienteOrderByDixonIdDesc(expedientes.dixonObtener(e.getDixonCodigo()));
        assertThat(lista).hasSize(5);
        assertThat(lista.get(0).getDixonCanal()).isEqualTo(DixonCanalAviso.CORREO);
        assertThat(DixonSeguimientoServicio.dixonCalcular(expedientes.dixonObtener(e.getDixonCodigo())).dixonEstimado()).isEqualTo("Entregado");
        DixonExpediente otro = expedientes.dixonMios().stream().filter(x -> x.getDixonGrupo() == DixonGrupoExpediente.TRAMITE && x.getDixonPago() == null).findFirst().orElseThrow();
        assertThatThrownBy(() -> admin.dixonObservar(otro.getDixonCodigo(), "corto")).hasMessage("Escribe al menos 15 caracteres.");
        admin.dixonObservar(otro.getDixonCodigo(), "Falta adjuntar la copia del DNI legible.");
        assertThat(expedientes.dixonObtener(otro.getDixonCodigo()).getDixonGrupo()).isEqualTo(DixonGrupoExpediente.OBSERVADO);
        assertThat(notificaciones.dixonListar(DixonRol.ESTUDIANTE).get(0).getDixonTexto()).contains("fue observado");
        DixonReserva r = reservas.dixonMias().stream().filter(x -> x.getDixonEstado() == DixonEstadoReserva.PENDIENTE).findFirst().orElseThrow();
        admin.dixonRechazarReserva(r.getDixonId(), "El auditorio está en mantenimiento.");
        assertThat(reservaRepository.findById(r.getDixonId()).orElseThrow().getDixonEstado()).isEqualTo(DixonEstadoReserva.RECHAZADO);
        DixonPago pagado = pagos.dixonMios().stream().filter(DixonPago::dixonEsPagado).findFirst().orElseThrow();
        admin.dixonReembolsar(pagado.getDixonCodigo());
        assertThat(pagos.dixonObtener(pagado.getDixonCodigo()).getDixonEstado()).isEqualTo(DixonEstadoPago.REEMBOLSADO);
    }

    @Test
    void sinConexionLaColaCreceLosPagosSeBloqueanYSeVacia() {
        conexion.dixonSimularSinConexion(true);
        assertThat(conexion.dixonSinConexion()).isTrue();
        favoritos.dixonAlternar(DixonTipoFavorito.EQUIPO, "e1");
        expedientes.dixonRegistrar("Traslado interno", "EP Ingeniería de Sistemas", "central", "Solicito traslado interno por motivos personales.", null);
        assertThat(conexion.dixonContarPendientes()).isEqualTo(2);
        String m = pagos.dixonMatricula().getDixonPago().getDixonCodigo();
        assertThatThrownBy(() -> pagos.dixonCompletar(m, DixonMetodoPago.YAPE)).hasMessage("Necesitas conexión para pagar");
        conexion.dixonSimularSinConexion(false);
        assertThat(conexion.dixonSincronizar()).isEqualTo(2);
        assertThat(conexion.dixonContarPendientes()).isZero();
        favoritos.dixonAlternar(DixonTipoFavorito.EQUIPO, "e2");
        assertThat(conexion.dixonContarPendientes()).isZero();
    }

    @Test
    void persistenciaDeIdaYVuelta() {
        DixonEspacio redes = catalogo.dixonEspacio("lab-redes").orElseThrow();
        LocalDate manana = DixonFechas.dixonHoy().plusDays(1);
        DixonReserva r = reservas.dixonReservar(redes, manana, "08:00 - 10:00", 20, "Práctica calificada de Redes");
        DixonReserva leida = reservaRepository.findById(r.getDixonId()).orElseThrow();
        assertThat(leida.getDixonEspacio().getDixonCodigo()).isEqualTo("lab-redes");
        assertThat(leida.getDixonFecha()).isEqualTo(manana);
        assertThat(leida.getDixonEstado()).isEqualTo(DixonEstadoReserva.PENDIENTE);
        assertThatThrownBy(() -> reservas.dixonReservar(redes, manana, "08:00 - 10:00", 5, "Otra clase del curso")).hasMessage("Ese horario ya está reservado. Elige otro.");
        ajustes.dixonCambiar(a -> a.setDixonIdioma("qu"));
        assertThat(ajustes.dixonObtener().getDixonIdioma()).isEqualTo("qu");
        assertThat(contexto.dixonCambiarEscuela("iel")).isEqualTo("EP Ingeniería Eléctrica · Sede Norte.");
        assertThat(contexto.dixonCodigoSede()).isEqualTo("norte");
        assertThat(contexto.dixonCambiarSede("central")).isEqualTo("Mostrando información de la Sede Central · todas las escuelas.");
        assertThat(contexto.dixonEscuela()).isNull();
        demo.dixonRestablecer();
        assertThat(reservaRepository.findById(r.getDixonId())).isEmpty();
        assertThat(ajustes.dixonObtener().getDixonIdioma()).isEqualTo("qu");
    }

    @Test
    void mapaLibrePorTerminarYOcupado() {
        DixonEspacio cnc = catalogo.dixonEspacio("taller-cnc").orElseThrow();
        assertThat(ocupacion.dixonEstado(cnc, 0, 0)).isEqualTo(DixonOcupacionServicio.DixonEstadoOcupacion.OCUPADO);
        DixonEspacio e = catalogo.dixonEspacios().stream().filter(x -> !x.isDixonFueraDeServicio()).filter(x -> DixonReservaServicio.dixonDisponibleSimulado(x.getDixonNombre(), 3, "08:00")
                && DixonReservaServicio.dixonDisponibleSimulado(x.getDixonNombre(), 3, "10:00")).findFirst().orElseThrow();
        assertThat(ocupacion.dixonEstado(e, 0, 3)).isEqualTo(DixonOcupacionServicio.DixonEstadoOcupacion.LIBRE);
        reservas.dixonReservar(e, DixonFechas.dixonHoy().plusDays(3), "10:00 - 12:00", 1, "Prueba del mapa en vivo");
        assertThat(ocupacion.dixonEstado(e, 0, 3)).isEqualTo(DixonOcupacionServicio.DixonEstadoOcupacion.POR_TERMINAR);
        assertThat(ocupacion.dixonEstado(e, 1, 3)).isEqualTo(DixonOcupacionServicio.DixonEstadoOcupacion.OCUPADO);
    }

    @Test
    void carneAceptaFranjaActualYVecinasYRechazaVencidoOAlterado() {
        long f = DixonCarneServicio.dixonFranja(DixonFechas.dixonMilis());
        String token = DixonCarneServicio.dixonToken("20240157", f);
        assertThat(carne.dixonVerificar(token, f).dixonCorrecto()).isTrue();
        assertThat(carne.dixonVerificar(token, f + 1).dixonCorrecto()).isTrue();
        assertThat(carne.dixonVerificar(token, f - 1).dixonCorrecto()).isTrue();
        assertThat(carne.dixonVerificar(token, f + 2).dixonMensaje()).isEqualTo("Código vencido. Pide al estudiante que muestre el código actual.");
        String alterado = "ANDRA-ID|20240157|" + f + "|ZZZZZZ";
        assertThat(carne.dixonVerificar(alterado, f).dixonMensaje()).isEqualTo("Firma inválida: el código fue alterado.");
        assertThat(carne.dixonVerificar("72458913", f).dixonMensaje()).isEqualTo("Estudiante encontrado (validación manual por código)");
        assertThat(carne.dixonVerificar("000", f).dixonCorrecto()).isFalse();
        assertThat(carne.dixonSimularLectura().isDixonConcedido()).isFalse();
    }

    @Test
    void analisisPorPeriodo() {
        contexto.dixonCambiarSede(DixonAjustes.DIXON_TODAS);
        DixonAnaliticaServicio.DixonAnalisis sem = analitica.dixonAnalizar("sem");
        DixonAnaliticaServicio.DixonAnalisis siete = analitica.dixonAnalizar("7");
        assertThat(sem.dixonTramitesYOrdenes()).isGreaterThanOrEqualTo(siete.dixonTramitesYOrdenes());
        assertThat(sem.dixonRecaudacion()).isGreaterThanOrEqualTo(siete.dixonRecaudacion());
        assertThat(sem.dixonSuperior()).hasSizeLessThanOrEqualTo(6);
        for (int i = 1; i < sem.dixonSuperior().size(); i++) {
            assertThat(sem.dixonSuperior().get(i - 1).dixonValor()).isGreaterThanOrEqualTo(sem.dixonSuperior().get(i).dixonValor());
        }
        assertThat(sem.dixonHallazgos()).hasSize(4);
        assertThat(sem.dixonCalor()[sem.dixonPico()[0]][sem.dixonPico()[1]]).isEqualTo(sem.dixonCalorMaximo());
        assertThat(sem.dixonSubutilizados()).hasSize(4);
        assertThat(sem.dixonDemandados()).hasSize(3);
        assertThat(sem.dixonPorSede()).hasSize(3);
    }
}
