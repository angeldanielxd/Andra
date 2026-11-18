package pe.andra.service;

import org.junit.jupiter.api.Test;
import pe.andra.config.AndraJsonRecursos;
import pe.andra.model.*;
import pe.andra.util.DixonDiasHabiles;
import pe.andra.util.DixonTarjetaJemm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ReglasTest {

    private static DixonTramite tramite(String entrega) {
        return DixonTramite.builder().dixonCodigo("t").dixonNombre("Constancia de estudios").dixonCategoria("Constancias").dixonMonto(new BigDecimal("15"))
                .dixonEntrega(entrega).dixonPublicos("Estudiante|Egresado").dixonPlantilla("constancia").build();
    }

    @Test
    void luhnYMarcaDeTarjeta() {
        assertThat(DixonTarjetaJemm.dixonLuhn("4111 1111 1111 1111")).isTrue();
        assertThat(DixonTarjetaJemm.dixonLuhn("4111 1111 1111 1112")).isFalse();
        assertThat(DixonTarjetaJemm.dixonMarca("4111")).isEqualTo(DixonMetodoPago.VISA);
        assertThat(DixonTarjetaJemm.dixonMarca("5500 0000")).isEqualTo(DixonMetodoPago.MASTERCARD);
        assertThat(DixonTarjetaJemm.dixonMarca("3782 822463")).isEqualTo(DixonMetodoPago.AMEX);
        assertThat(DixonTarjetaJemm.dixonMarca("3056 9309")).isEqualTo(DixonMetodoPago.DINERS);
        assertThat(DixonTarjetaJemm.dixonMarca("6011")).isNull();
        assertThat(DixonTarjetaJemm.dixonFormatear("4111111111111111")).isEqualTo("4111 1111 1111 1111");
        assertThat(DixonTarjetaJemm.dixonVencimientoValido("12/30", YearMonth.of(2026, 10))).isTrue();
        assertThat(DixonTarjetaJemm.dixonVencimientoValido("09/26", YearMonth.of(2026, 10))).isFalse();
        assertThat(DixonPagoServicio.dixonValidarTarjeta("4111 1111 1111 1112", "A", "12/30", "123", "a@b.pe", YearMonth.of(2026, 10)))
                .containsEntry("numero", "El número de tarjeta no es válido. Revísalo.");
    }

    @Test
    void validacionesDeYape() {
        assertThat(DixonPagoServicio.dixonValidarYape("912345678", "123456")).isEmpty();
        assertThat(DixonPagoServicio.dixonValidarYape("851234567", "123456")).containsEntry("celular", "Celular de 9 dígitos que empiece con 9.");
        assertThat(DixonPagoServicio.dixonValidarYape("912345678", "12345")).containsEntry("codigo", "El código tiene 6 dígitos.");
        assertThat(DixonPagoServicio.dixonValidarYape("", "")).containsKeys("celular", "codigo");
    }

    @Test
    void recojoConDiasHabilesYCorteDeLasQuince() {
        LocalDate martes = LocalDate.of(2026, 10, 20);
        DixonPagoServicio.DixonRecojo antes = DixonPagoServicio.dixonRecojo(tramite("mismo"), martes.atTime(14, 59));
        assertThat(antes.dixonFecha()).isEqualTo(martes);
        assertThat(antes.dixonTexto()).startsWith("Hoy, ").endsWith("desde las 16:00");
        DixonPagoServicio.DixonRecojo despues = DixonPagoServicio.dixonRecojo(tramite("mismo"), martes.atTime(15, 0));
        assertThat(despues.dixonFecha()).isEqualTo(martes.plusDays(1));
        assertThat(despues.dixonTexto()).endsWith("desde las 09:00");
        LocalDate viernes = LocalDate.of(2026, 10, 23);
        assertThat(DixonPagoServicio.dixonRecojo(tramite("mismo"), viernes.atTime(16, 0)).dixonFecha()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(DixonPagoServicio.dixonRecojo(tramite("mismo"), LocalDate.of(2026, 10, 24).atTime(9, 0)).dixonFecha()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(DixonPagoServicio.dixonRecojo(tramite("3"), viernes.atTime(9, 0)).dixonFecha()).isEqualTo(LocalDate.of(2026, 10, 28));
        assertThat(DixonPagoServicio.dixonRecojo(tramite("carne"), martes.atTime(9, 0)).dixonFecha()).isEqualTo(DixonDiasHabiles.dixonSumar(martes, 10));
        assertThat(DixonPagoServicio.dixonRecojo(tramite("inmediato"), martes.atTime(9, 0)).dixonFecha()).isNull();
        assertThat(DixonDiasHabiles.dixonSumar(martes, 15)).isEqualTo(LocalDate.of(2026, 11, 10));
    }

    @Test
    void totalesDeOrdenes() {
        DixonTramite silabos = tramite("mismo");
        silabos.setDixonPorCantidad(true);
        silabos.setDixonMonto(new BigDecimal("5"));
        List<DixonElementoPago> items = DixonPagoServicio.dixonElementosPara(silabos, 3);
        assertThat(items.get(0).getDixonConcepto()).endsWith("(3 cursos)");
        assertThat(DixonPagoServicio.dixonTotalOrden(items)).isEqualByComparingTo("15");
        assertThat(DixonPagoServicio.dixonElementosPara(silabos, 50).get(0).getDixonCantidad()).isEqualTo(20);
        DixonPago p = DixonPago.builder().dixonElementos(List.of(new DixonElementoPago("A", 1, new BigDecimal("60")), new DixonElementoPago("B", 1, new BigDecimal("20")))).build();
        assertThat(p.dixonObtenerTotal()).isEqualByComparingTo("80");
    }

    @Test
    void carneCambiaCadaQuinceSegundosYValidaFranjaYFirma() {
        long t = 1_790_000_000_000L;
        long f = DixonCarneServicio.dixonFranja(t);
        assertThat(DixonCarneServicio.dixonFranja(t + 15000)).isEqualTo(f + 1);
        assertThat(DixonCarneServicio.dixonToken("20240157", f)).isNotEqualTo(DixonCarneServicio.dixonToken("20240157", f + 1));
        assertThat(DixonCarneServicio.dixonFirma("20240157", f)).hasSize(6).matches("[0-9A-Z]{6}");
        String token = DixonCarneServicio.dixonToken("20240157", f);
        Matcher m = Pattern.compile("^ANDRA-ID\\|(\\w+)\\|(\\d+)\\|(\\w+)$").matcher(token);
        assertThat(m.matches()).isTrue();
        String alterado = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");
        assertThat(DixonCarneServicio.dixonFirma("20240157", f)).isNotEqualTo(alterado.substring(alterado.lastIndexOf('|') + 1));
        assertThat(DixonCarneServicio.dixonSegundosRestantes(f * 15000)).isEqualTo(15);
    }

    @Test
    void reglasDeContexto() {
        DixonSede central = DixonSede.builder().dixonCodigo("central").build();
        DixonSede norte = DixonSede.builder().dixonCodigo("norte").build();
        DixonEscuelaProfesional iss = DixonEscuelaProfesional.builder().dixonCodigo("isi").dixonSede(central).build();
        DixonEscuelaProfesional imc = DixonEscuelaProfesional.builder().dixonCodigo("imc").dixonSede(central).build();
        assertThat(DixonContextoServicio.dixonPertenece("central", null, central, iss)).isTrue();
        assertThat(DixonContextoServicio.dixonPertenece("central", "isi", central, null)).isTrue();
        assertThat(DixonContextoServicio.dixonPertenece("central", "isi", central, iss)).isTrue();
        assertThat(DixonContextoServicio.dixonPertenece("central", "isi", central, imc)).isFalse();
        assertThat(DixonContextoServicio.dixonPertenece("central", null, norte, null)).isFalse();
        assertThat(DixonContextoServicio.dixonPertenece("all", null, norte, null)).isTrue();
    }

    @Test
    void filtrosDeDocumentosYRangos() {
        DixonEscuelaProfesional iss = DixonEscuelaProfesional.builder().dixonCodigo("isi").dixonNombre("Ingeniería de Sistemas").build();
        DixonDocumento a = DixonDocumento.builder().dixonCodigo("a").dixonNombre("Reglamento de matrícula").dixonDescripcion("Normas").dixonTipo("Reglamentos")
                .dixonDependencia("Vicerrectorado Académico").dixonPublico("Estudiantes").dixonFecha(LocalDate.of(2026, 1, 20)).build();
        DixonDocumento b = DixonDocumento.builder().dixonCodigo("b").dixonNombre("Sílabo Algoritmos").dixonDescripcion("Contenidos").dixonTipo("Sílabos")
                .dixonDependencia("Ingeniería de Sistemas").dixonPublico("Estudiantes").dixonFecha(LocalDate.of(2025, 8, 3)).dixonEscuela(iss).build();
        LocalDate hoy = LocalDate.of(2026, 10, 21);
        List<DixonDocumento> todos = List.of(a, b);
        assertThat(DixonDocumentoServicio.dixonFiltrar(todos, new DixonDocumentoServicio.DixonCriterio("silabo", "", "", "", "any", null, null), null, hoy)).containsExactly(b);
        assertThat(DixonDocumentoServicio.dixonFiltrar(todos, new DixonDocumentoServicio.DixonCriterio("", "", "", "", "year", null, null), null, hoy)).containsExactly(a);
        assertThat(DixonDocumentoServicio.dixonFiltrar(todos, new DixonDocumentoServicio.DixonCriterio("", "", "", "", "2025", null, null), null, hoy)).containsExactly(b);
        assertThat(DixonDocumentoServicio.dixonFiltrar(todos, new DixonDocumentoServicio.DixonCriterio("", "", "", "", "custom", LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 5)), null, hoy)).containsExactly(b);
        assertThat(DixonDocumentoServicio.dixonValidarRangoPersonalizado(new DixonDocumentoServicio.DixonCriterio("", "", "", "", "custom",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1)))).isEqualTo("La fecha final debe ser posterior a la inicial.");
        assertThat(DixonDocumentoServicio.dixonOrdenar(todos, "old", false, java.util.Set.of())).containsExactly(b, a);
    }

    @Test
    void filtrosAvanzadosDelAdministrador() {
        record Fila(LocalDate fecha, String codigo, String escuela, String tipo) {
        }
        List<Fila> filas = List.of(new Fila(LocalDate.of(2026, 10, 1), "20240157 72458913", "isi", "Estudiante"),
                new Fila(LocalDate.of(2026, 10, 15), "D-10452 40125874", "gpu", "Docente"));
        DixonEstadoFiltro e = new DixonEstadoFiltro();
        e.dixonObtenerAvanzados().put("from", "2026-10-10");
        DixonFiltroServicio.DixonAccesores<Fila> acc = new DixonFiltroServicio.DixonAccesores<>(Fila::fecha, Fila::codigo,
                java.util.Map.of("fac", Fila::escuela, "tipo", Fila::tipo));
        assertThat(DixonFiltroServicio.dixonAplicarAvanzado(filas, e, acc)).extracting(Fila::tipo).containsExactly("Docente");
        e.dixonLimpiar();
        e.dixonObtenerAvanzados().put("code", "72458913");
        assertThat(DixonFiltroServicio.dixonAplicarAvanzado(filas, e, acc)).hasSize(1);
        e.dixonLimpiar();
        e.dixonObtenerAvanzados().put("fac", "gpu");
        e.dixonObtenerAvanzados().put("tipo", "Docente");
        assertThat(DixonFiltroServicio.dixonAplicarAvanzado(filas, e, acc)).hasSize(1);
        assertThat(e.dixonCantidad()).isEqualTo(2);
        assertThat(DixonFiltroServicio.dixonValidarRango("2026-10-10", "2026-10-01")).isEqualTo("La fecha “Hasta” debe ser posterior a “Desde”.");
    }

    @Test
    void permisosPorRol() {
        assertThat(DixonSesionServicio.dixonPuedeAcceder(DixonRol.ESTUDIANTE, "admin")).isFalse();
        assertThat(DixonSesionServicio.dixonPuedeAcceder(DixonRol.ESTUDIANTE, "a-datos")).isFalse();
        assertThat(DixonSesionServicio.dixonPuedeAcceder(DixonRol.ESTUDIANTE, "carne")).isTrue();
        assertThat(DixonSesionServicio.dixonPuedeAcceder(DixonRol.ADMINISTRADOR, "admin")).isTrue();
        assertThat(DixonSesionServicio.dixonPuedeAcceder(DixonRol.ADMINISTRADOR, "pay")).isFalse();
        assertThat(DixonSesionServicio.dixonPuedeAcceder(null, "inicio")).isFalse();
        assertThat(DixonSesionServicio.dixonRutas(DixonRol.ESTUDIANTE)).hasSize(8);
        assertThat(DixonSesionServicio.dixonRutas(DixonRol.ADMINISTRADOR)).hasSize(6);
    }

    private static DixonExpediente expediente(DixonClaseExpediente clase, DixonGrupoExpediente grupo, DixonPago pago, String estado) {
        DixonExpediente e = DixonExpediente.builder().dixonCodigo("SOL-2026-000001").dixonClase(clase).dixonGrupo(grupo).dixonPago(pago).dixonEstado(estado).dixonPaso(1)
                .dixonSede(DixonSede.builder().dixonNombre("Central").build()).dixonFecha(LocalDateTime.of(2026, 10, 20, 9, 0)).build();
        return e;
    }

    @Test
    void etapasDeSeguimientoParaCadaTipo() {
        DixonPago pendiente = DixonPago.builder().dixonEstado(DixonEstadoPago.PENDIENTE).build();
        DixonSeguimientoServicio.DixonSeguimientoCjsv a = DixonSeguimientoServicio.dixonCalcular(expediente(DixonClaseExpediente.DOC, DixonGrupoExpediente.TRAMITE, pendiente, "Pago pendiente"));
        assertThat(a.dixonEtapas()).hasSize(6);
        assertThat(a.dixonActual()).isEqualTo(1);
        assertThat(a.dixonEstimado()).isEqualTo("Esperando tu pago en Andra Pay");
        DixonPago pagado = DixonPago.builder().dixonEstado(DixonEstadoPago.PAGADO).dixonPagado(LocalDateTime.of(2026, 10, 20, 10, 0)).build();
        assertThat(DixonSeguimientoServicio.dixonCalcular(expediente(DixonClaseExpediente.DOC, DixonGrupoExpediente.LISTO, pagado, "Listo")).dixonActual()).isEqualTo(4);
        assertThat(DixonSeguimientoServicio.dixonCalcular(expediente(DixonClaseExpediente.DOC, DixonGrupoExpediente.LISTO, pagado, "Entregado al solicitante")).dixonEstimado()).isEqualTo("Entregado");
        DixonSeguimientoServicio.DixonSeguimientoCjsv sin = DixonSeguimientoServicio.dixonCalcular(expediente(DixonClaseExpediente.DOC, DixonGrupoExpediente.TRAMITE, null, "En revisión"));
        assertThat(sin.dixonEtapas()).extracting(DixonSeguimientoServicio.DixonEtapa::dixonTitulo).containsExactly("Recibido", "En revisión", "En firma",
                "Listo en ventanilla " + sin.dixonVentanilla(), "Entregado");
        assertThat(sin.dixonEstimado()).isEqualTo("Estimado: 2 a 3 días hábiles");
        DixonSeguimientoServicio.DixonSeguimientoCjsv obs = DixonSeguimientoServicio.dixonCalcular(expediente(DixonClaseExpediente.DOC, DixonGrupoExpediente.OBSERVADO, null, "Observado"));
        assertThat(obs.dixonObservado()).isTrue();
        assertThat(obs.dixonEstimado()).isEqualTo("Detenido: tienes una observación por subsanar");
        DixonSeguimientoServicio.DixonSeguimientoCjsv lab = DixonSeguimientoServicio.dixonCalcular(expediente(DixonClaseExpediente.LAB, DixonGrupoExpediente.LISTO, null, "Confirmado"));
        assertThat(lab.dixonEtapas()).extracting(DixonSeguimientoServicio.DixonEtapa::dixonTitulo).containsExactly("Solicitado", "Aprobado", "Listo para usar", "Finalizado");
        assertThat(lab.dixonActual()).isEqualTo(2);
        assertThat(DixonSeguimientoServicio.dixonCalcular(expediente(DixonClaseExpediente.EQ, DixonGrupoExpediente.ANULADO, null, "Cancelado")).dixonActual()).isZero();
        assertThat(DixonSeguimientoServicio.dixonVentanilla("SOL-2026-084920")).isBetween(1, 6).isEqualTo(DixonSeguimientoServicio.dixonVentanilla("SOL-2026-084920"));
    }

    @Test
    void mapaDeCalorYCeldaPico() {
        int[][] calor = DixonAnaliticaServicio.dixonMapaCalor(List.of());
        assertThat(calor).hasDimensions(5, 5);
        int[] pico = DixonAnaliticaServicio.dixonPico(calor);
        int max = 0;
        for (int[] fila : calor) {
            for (int v : fila) {
                max = Math.max(max, v);
            }
        }
        assertThat(calor[pico[0]][pico[1]]).isEqualTo(max);
        assertThat(DixonAnaliticaServicio.dixonNivel(max, max)).isEqualTo(4);
        assertThat(DixonAnaliticaServicio.dixonNivel(0, max)).isZero();
        DixonPago lunes = DixonPago.builder().dixonCreado(LocalDateTime.of(2026, 10, 19, 10, 30)).build();
        assertThat(DixonAnaliticaServicio.dixonMapaCalor(List.of(lunes))[0][1]).isEqualTo(calor[0][1] + 3);
    }

    @Test
    void franjaDelMapaEnVivo() {
        assertThat(DixonOcupacionServicio.dixonFranjaPara(LocalDateTime.of(2026, 10, 21, 10, 30))).isEqualTo(new DixonOcupacionServicio.DixonFranjaBnps(1, 0, true));
        assertThat(DixonOcupacionServicio.dixonFranjaPara(LocalDateTime.of(2026, 10, 21, 19, 0))).isEqualTo(new DixonOcupacionServicio.DixonFranjaBnps(0, 1, false));
        assertThat(DixonOcupacionServicio.dixonFranjaPara(LocalDateTime.of(2026, 10, 24, 10, 0)).dixonEnVivo()).isFalse();
        assertThat(DixonOcupacionServicio.dixonNombreCorto("Laboratorio de Suelos y Calidad Ambiental")).isEqualTo("Suelos y Calidad Ambiental");
    }

    @Test
    void idiomasConservanMarcadoresYCaenAlCastellano() {
        DixonIdiomaServicio idioma = new DixonIdiomaServicio(new AndraJsonRecursos());
        idioma.dixonCargar();
        assertThat(idioma.dixonTotalTextos()).isGreaterThan(1500);
        Pattern marca = Pattern.compile("\\{\\d+}");
        for (String clave : idioma.dixonClaves()) {
            List<String> esperadas = marca.matcher(clave).results().map(r -> r.group()).sorted().toList();
            for (String l : List.of("en", "qu", "ay")) {
                String t = idioma.dixonDirecta(clave, l);
                assertThat(t).as(l + ": " + clave).isNotBlank();
                assertThat(marca.matcher(t).results().map(r -> r.group()).sorted().toList()).as(l + ": " + clave).isEqualTo(esperadas);
            }
        }
        for (String s : List.of("Inicio", "Documentos", "Reservas", "Equipos", "Mis Solicitudes", "Ayuda", "Favoritos", "Panel", "Análisis",
                "Trámites", "Transacciones", "Préstamos", "Notificaciones", "Idioma", "Cerrar sesión", "Ingresa a tu cuenta", "Mi carné digital",
                "Seguimiento en vivo", "Buscar disponibilidad", "Filtros rápidos", "Lo que dicen los datos")) {
            for (String l : List.of("en", "qu", "ay")) {
                assertThat(idioma.dixonTraducir(s, l)).as(l + ": " + s).isNotEqualTo(s);
            }
        }
        assertThat(idioma.dixonTraducir("Tienes 3 pagos pendientes, incluida tu matrícula 2026-2.", "es")).startsWith("Tienes 3");
        assertThat(idioma.dixonTraducir("Se renueva en 7 s", "en")).isEqualTo("Renews in 7 s");
        assertThat(idioma.dixonTraducir("Texto inexistente para la prueba", "en")).isEqualTo("Texto inexistente para la prueba");
        assertThat(idioma.dixonTraducir("Martes, 20 de octubre, 2026", "en")).isEqualTo("Tuesday, October 20, 2026");
        assertThat(idioma.dixonTraducir("Martes, 20 de octubre, 2026", "qu")).endsWith("20 octubre killa, 2026");
        assertThat(idioma.dixonTraducir("Martes, 20 de octubre, 2026", "ay")).contains("octubre phaxsi");
    }
}
