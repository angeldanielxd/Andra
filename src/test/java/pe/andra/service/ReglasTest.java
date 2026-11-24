package pe.andra.service;

import org.junit.jupiter.api.Test;
import pe.andra.config.AndraJsonRecursos;
import pe.andra.model.*;
import pe.andra.util.AndraDiasHabiles;
import pe.andra.util.AndraTarjetaJemm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ReglasTest {

    private static AndraTramite tramite(String entrega) {
        return AndraTramite.builder().andraCodigo("t").andraNombre("Constancia de estudios").andraCategoria("Constancias").andraMonto(new BigDecimal("15"))
                .andraEntrega(entrega).andraPublicos("Estudiante|Egresado").andraPlantilla("constancia").build();
    }

    @Test
    void luhnYMarcaDeTarjeta() {
        assertThat(AndraTarjetaJemm.andraLuhn("4111 1111 1111 1111")).isTrue();
        assertThat(AndraTarjetaJemm.andraLuhn("4111 1111 1111 1112")).isFalse();
        assertThat(AndraTarjetaJemm.andraMarca("4111")).isEqualTo(AndraMetodoPago.VISA);
        assertThat(AndraTarjetaJemm.andraMarca("5500 0000")).isEqualTo(AndraMetodoPago.MASTERCARD);
        assertThat(AndraTarjetaJemm.andraMarca("3782 822463")).isEqualTo(AndraMetodoPago.AMEX);
        assertThat(AndraTarjetaJemm.andraMarca("3056 9309")).isEqualTo(AndraMetodoPago.DINERS);
        assertThat(AndraTarjetaJemm.andraMarca("6011")).isNull();
        assertThat(AndraTarjetaJemm.andraFormatear("4111111111111111")).isEqualTo("4111 1111 1111 1111");
        assertThat(AndraTarjetaJemm.andraVencimientoValido("12/30", YearMonth.of(2026, 10))).isTrue();
        assertThat(AndraTarjetaJemm.andraVencimientoValido("09/26", YearMonth.of(2026, 10))).isFalse();
        assertThat(AndraPagoServicio.andraValidarTarjeta("4111 1111 1111 1112", "A", "12/30", "123", "a@b.pe", YearMonth.of(2026, 10)))
                .containsEntry("numero", "El número de tarjeta no es válido. Revísalo.");
    }

    @Test
    void validacionesDeYape() {
        assertThat(AndraPagoServicio.andraValidarYape("912345678", "123456")).isEmpty();
        assertThat(AndraPagoServicio.andraValidarYape("851234567", "123456")).containsEntry("celular", "Celular de 9 dígitos que empiece con 9.");
        assertThat(AndraPagoServicio.andraValidarYape("912345678", "12345")).containsEntry("codigo", "El código tiene 6 dígitos.");
        assertThat(AndraPagoServicio.andraValidarYape("", "")).containsKeys("celular", "codigo");
    }

    @Test
    void recojoConDiasHabilesYCorteDeLasQuince() {
        LocalDate martes = LocalDate.of(2026, 10, 20);
        AndraPagoServicio.AndraRecojo antes = AndraPagoServicio.andraRecojo(tramite("mismo"), martes.atTime(14, 59));
        assertThat(antes.andraFecha()).isEqualTo(martes);
        assertThat(antes.andraTexto()).startsWith("Hoy, ").endsWith("desde las 16:00");
        AndraPagoServicio.AndraRecojo despues = AndraPagoServicio.andraRecojo(tramite("mismo"), martes.atTime(15, 0));
        assertThat(despues.andraFecha()).isEqualTo(martes.plusDays(1));
        assertThat(despues.andraTexto()).endsWith("desde las 09:00");
        LocalDate viernes = LocalDate.of(2026, 10, 23);
        assertThat(AndraPagoServicio.andraRecojo(tramite("mismo"), viernes.atTime(16, 0)).andraFecha()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(AndraPagoServicio.andraRecojo(tramite("mismo"), LocalDate.of(2026, 10, 24).atTime(9, 0)).andraFecha()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(AndraPagoServicio.andraRecojo(tramite("3"), viernes.atTime(9, 0)).andraFecha()).isEqualTo(LocalDate.of(2026, 10, 28));
        assertThat(AndraPagoServicio.andraRecojo(tramite("carne"), martes.atTime(9, 0)).andraFecha()).isEqualTo(AndraDiasHabiles.andraSumar(martes, 10));
        assertThat(AndraPagoServicio.andraRecojo(tramite("inmediato"), martes.atTime(9, 0)).andraFecha()).isNull();
        assertThat(AndraDiasHabiles.andraSumar(martes, 15)).isEqualTo(LocalDate.of(2026, 11, 10));
    }

    @Test
    void totalesDeOrdenes() {
        AndraTramite silabos = tramite("mismo");
        silabos.setAndraPorCantidad(true);
        silabos.setAndraMonto(new BigDecimal("5"));
        List<AndraElementoPago> items = AndraPagoServicio.andraElementosPara(silabos, 3);
        assertThat(items.get(0).getAndraConcepto()).endsWith("(3 cursos)");
        assertThat(AndraPagoServicio.andraTotalOrden(items)).isEqualByComparingTo("15");
        assertThat(AndraPagoServicio.andraElementosPara(silabos, 50).get(0).getAndraCantidad()).isEqualTo(20);
        AndraPago p = AndraPago.builder().andraElementos(List.of(new AndraElementoPago("A", 1, new BigDecimal("60")), new AndraElementoPago("B", 1, new BigDecimal("20")))).build();
        assertThat(p.andraObtenerTotal()).isEqualByComparingTo("80");
    }

    @Test
    void carneCambiaCadaQuinceSegundosYValidaFranjaYFirma() {
        long t = 1_790_000_000_000L;
        long f = AndraCarneServicio.andraFranja(t);
        assertThat(AndraCarneServicio.andraFranja(t + 15000)).isEqualTo(f + 1);
        assertThat(AndraCarneServicio.andraToken("20240157", f)).isNotEqualTo(AndraCarneServicio.andraToken("20240157", f + 1));
        assertThat(AndraCarneServicio.andraFirma("20240157", f)).hasSize(6).matches("[0-9A-Z]{6}");
        String token = AndraCarneServicio.andraToken("20240157", f);
        Matcher m = Pattern.compile("^ANDRA-ID\\|(\\w+)\\|(\\d+)\\|(\\w+)$").matcher(token);
        assertThat(m.matches()).isTrue();
        String alterado = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");
        assertThat(AndraCarneServicio.andraFirma("20240157", f)).isNotEqualTo(alterado.substring(alterado.lastIndexOf('|') + 1));
        assertThat(AndraCarneServicio.andraSegundosRestantes(f * 15000)).isEqualTo(15);
    }

    @Test
    void reglasDeContexto() {
        AndraSede central = AndraSede.builder().andraCodigo("central").build();
        AndraSede norte = AndraSede.builder().andraCodigo("norte").build();
        AndraEscuelaProfesional iss = AndraEscuelaProfesional.builder().andraCodigo("isi").andraSede(central).build();
        AndraEscuelaProfesional imc = AndraEscuelaProfesional.builder().andraCodigo("imc").andraSede(central).build();
        assertThat(AndraContextoServicio.andraPertenece("central", null, central, iss)).isTrue();
        assertThat(AndraContextoServicio.andraPertenece("central", "isi", central, null)).isTrue();
        assertThat(AndraContextoServicio.andraPertenece("central", "isi", central, iss)).isTrue();
        assertThat(AndraContextoServicio.andraPertenece("central", "isi", central, imc)).isFalse();
        assertThat(AndraContextoServicio.andraPertenece("central", null, norte, null)).isFalse();
        assertThat(AndraContextoServicio.andraPertenece("all", null, norte, null)).isTrue();
    }

    @Test
    void filtrosDeDocumentosYRangos() {
        AndraEscuelaProfesional iss = AndraEscuelaProfesional.builder().andraCodigo("isi").andraNombre("Ingeniería de Sistemas").build();
        AndraDocumento a = AndraDocumento.builder().andraCodigo("a").andraNombre("Reglamento de matrícula").andraDescripcion("Normas").andraTipo("Reglamentos")
                .andraDependencia("Vicerrectorado Académico").andraPublico("Estudiantes").andraFecha(LocalDate.of(2026, 1, 20)).build();
        AndraDocumento b = AndraDocumento.builder().andraCodigo("b").andraNombre("Sílabo Algoritmos").andraDescripcion("Contenidos").andraTipo("Sílabos")
                .andraDependencia("Ingeniería de Sistemas").andraPublico("Estudiantes").andraFecha(LocalDate.of(2025, 8, 3)).andraEscuela(iss).build();
        LocalDate hoy = LocalDate.of(2026, 10, 21);
        List<AndraDocumento> todos = List.of(a, b);
        assertThat(AndraDocumentoServicio.andraFiltrar(todos, new AndraDocumentoServicio.AndraCriterio("silabo", "", "", "", "any", null, null), null, hoy)).containsExactly(b);
        assertThat(AndraDocumentoServicio.andraFiltrar(todos, new AndraDocumentoServicio.AndraCriterio("", "", "", "", "year", null, null), null, hoy)).containsExactly(a);
        assertThat(AndraDocumentoServicio.andraFiltrar(todos, new AndraDocumentoServicio.AndraCriterio("", "", "", "", "2025", null, null), null, hoy)).containsExactly(b);
        assertThat(AndraDocumentoServicio.andraFiltrar(todos, new AndraDocumentoServicio.AndraCriterio("", "", "", "", "custom", LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 5)), null, hoy)).containsExactly(b);
        assertThat(AndraDocumentoServicio.andraValidarRangoPersonalizado(new AndraDocumentoServicio.AndraCriterio("", "", "", "", "custom",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1)))).isEqualTo("La fecha final debe ser posterior a la inicial.");
        assertThat(AndraDocumentoServicio.andraOrdenar(todos, "old", false, java.util.Set.of())).containsExactly(b, a);
    }

    @Test
    void filtrosAvanzadosDelAdministrador() {
        record Fila(LocalDate fecha, String codigo, String escuela, String tipo) {
        }
        List<Fila> filas = List.of(new Fila(LocalDate.of(2026, 10, 1), "20240157 72458913", "isi", "Estudiante"),
                new Fila(LocalDate.of(2026, 10, 15), "D-10452 40125874", "gpu", "Docente"));
        AndraEstadoFiltro e = new AndraEstadoFiltro();
        e.andraObtenerAvanzados().put("from", "2026-10-10");
        AndraFiltroServicio.AndraAccesores<Fila> acc = new AndraFiltroServicio.AndraAccesores<>(Fila::fecha, Fila::codigo,
                java.util.Map.of("fac", Fila::escuela, "tipo", Fila::tipo));
        assertThat(AndraFiltroServicio.andraAplicarAvanzado(filas, e, acc)).extracting(Fila::tipo).containsExactly("Docente");
        e.andraLimpiar();
        e.andraObtenerAvanzados().put("code", "72458913");
        assertThat(AndraFiltroServicio.andraAplicarAvanzado(filas, e, acc)).hasSize(1);
        e.andraLimpiar();
        e.andraObtenerAvanzados().put("fac", "gpu");
        e.andraObtenerAvanzados().put("tipo", "Docente");
        assertThat(AndraFiltroServicio.andraAplicarAvanzado(filas, e, acc)).hasSize(1);
        assertThat(e.andraCantidad()).isEqualTo(2);
        assertThat(AndraFiltroServicio.andraValidarRango("2026-10-10", "2026-10-01")).isEqualTo("La fecha “Hasta” debe ser posterior a “Desde”.");
    }

    @Test
    void permisosPorRol() {
        assertThat(AndraSesionServicio.andraPuedeAcceder(AndraRol.ESTUDIANTE, "admin")).isFalse();
        assertThat(AndraSesionServicio.andraPuedeAcceder(AndraRol.ESTUDIANTE, "a-datos")).isFalse();
        assertThat(AndraSesionServicio.andraPuedeAcceder(AndraRol.ESTUDIANTE, "carne")).isTrue();
        assertThat(AndraSesionServicio.andraPuedeAcceder(AndraRol.ADMINISTRADOR, "admin")).isTrue();
        assertThat(AndraSesionServicio.andraPuedeAcceder(AndraRol.ADMINISTRADOR, "pay")).isFalse();
        assertThat(AndraSesionServicio.andraPuedeAcceder(null, "inicio")).isFalse();
        assertThat(AndraSesionServicio.andraRutas(AndraRol.ESTUDIANTE)).hasSize(8);
        assertThat(AndraSesionServicio.andraRutas(AndraRol.ADMINISTRADOR)).hasSize(6);
    }

    private static AndraExpediente expediente(AndraClaseExpediente clase, AndraGrupoExpediente grupo, AndraPago pago, String estado) {
        AndraExpediente e = AndraExpediente.builder().andraCodigo("SOL-2026-000001").andraClase(clase).andraGrupo(grupo).andraPago(pago).andraEstado(estado).andraPaso(1)
                .andraSede(AndraSede.builder().andraNombre("Central").build()).andraFecha(LocalDateTime.of(2026, 10, 20, 9, 0)).build();
        return e;
    }

    @Test
    void etapasDeSeguimientoParaCadaTipo() {
        AndraPago pendiente = AndraPago.builder().andraEstado(AndraEstadoPago.PENDIENTE).build();
        AndraSeguimientoServicio.AndraSeguimientoCjsv a = AndraSeguimientoServicio.andraCalcular(expediente(AndraClaseExpediente.DOC, AndraGrupoExpediente.TRAMITE, pendiente, "Pago pendiente"));
        assertThat(a.andraEtapas()).hasSize(6);
        assertThat(a.andraActual()).isEqualTo(1);
        assertThat(a.andraEstimado()).isEqualTo("Esperando tu pago en Andra Pay");
        AndraPago pagado = AndraPago.builder().andraEstado(AndraEstadoPago.PAGADO).andraPagado(LocalDateTime.of(2026, 10, 20, 10, 0)).build();
        assertThat(AndraSeguimientoServicio.andraCalcular(expediente(AndraClaseExpediente.DOC, AndraGrupoExpediente.LISTO, pagado, "Listo")).andraActual()).isEqualTo(4);
        assertThat(AndraSeguimientoServicio.andraCalcular(expediente(AndraClaseExpediente.DOC, AndraGrupoExpediente.LISTO, pagado, "Entregado al solicitante")).andraEstimado()).isEqualTo("Entregado");
        AndraSeguimientoServicio.AndraSeguimientoCjsv sin = AndraSeguimientoServicio.andraCalcular(expediente(AndraClaseExpediente.DOC, AndraGrupoExpediente.TRAMITE, null, "En revisión"));
        assertThat(sin.andraEtapas()).extracting(AndraSeguimientoServicio.AndraEtapa::andraTitulo).containsExactly("Recibido", "En revisión", "En firma",
                "Listo en ventanilla " + sin.andraVentanilla(), "Entregado");
        assertThat(sin.andraEstimado()).isEqualTo("Estimado: 2 a 3 días hábiles");
        AndraSeguimientoServicio.AndraSeguimientoCjsv obs = AndraSeguimientoServicio.andraCalcular(expediente(AndraClaseExpediente.DOC, AndraGrupoExpediente.OBSERVADO, null, "Observado"));
        assertThat(obs.andraObservado()).isTrue();
        assertThat(obs.andraEstimado()).isEqualTo("Detenido: tienes una observación por subsanar");
        AndraSeguimientoServicio.AndraSeguimientoCjsv lab = AndraSeguimientoServicio.andraCalcular(expediente(AndraClaseExpediente.LAB, AndraGrupoExpediente.LISTO, null, "Confirmado"));
        assertThat(lab.andraEtapas()).extracting(AndraSeguimientoServicio.AndraEtapa::andraTitulo).containsExactly("Solicitado", "Aprobado", "Listo para usar", "Finalizado");
        assertThat(lab.andraActual()).isEqualTo(2);
        assertThat(AndraSeguimientoServicio.andraCalcular(expediente(AndraClaseExpediente.EQ, AndraGrupoExpediente.ANULADO, null, "Cancelado")).andraActual()).isZero();
        assertThat(AndraSeguimientoServicio.andraVentanilla("SOL-2026-084920")).isBetween(1, 6).isEqualTo(AndraSeguimientoServicio.andraVentanilla("SOL-2026-084920"));
    }

    @Test
    void mapaDeCalorYCeldaPico() {
        int[][] calor = AndraAnaliticaServicio.andraMapaCalor(List.of());
        assertThat(calor).hasDimensions(5, 5);
        int[] pico = AndraAnaliticaServicio.andraPico(calor);
        int max = 0;
        for (int[] fila : calor) {
            for (int v : fila) {
                max = Math.max(max, v);
            }
        }
        assertThat(calor[pico[0]][pico[1]]).isEqualTo(max);
        assertThat(AndraAnaliticaServicio.andraNivel(max, max)).isEqualTo(4);
        assertThat(AndraAnaliticaServicio.andraNivel(0, max)).isZero();
        AndraPago lunes = AndraPago.builder().andraCreado(LocalDateTime.of(2026, 10, 19, 10, 30)).build();
        assertThat(AndraAnaliticaServicio.andraMapaCalor(List.of(lunes))[0][1]).isEqualTo(calor[0][1] + 3);
    }

    @Test
    void franjaDelMapaEnVivo() {
        assertThat(AndraOcupacionServicio.andraFranjaPara(LocalDateTime.of(2026, 10, 21, 10, 30))).isEqualTo(new AndraOcupacionServicio.AndraFranjaBnps(1, 0, true));
        assertThat(AndraOcupacionServicio.andraFranjaPara(LocalDateTime.of(2026, 10, 21, 19, 0))).isEqualTo(new AndraOcupacionServicio.AndraFranjaBnps(0, 1, false));
        assertThat(AndraOcupacionServicio.andraFranjaPara(LocalDateTime.of(2026, 10, 24, 10, 0)).andraEnVivo()).isFalse();
        assertThat(AndraOcupacionServicio.andraNombreCorto("Laboratorio de Suelos y Calidad Ambiental")).isEqualTo("Suelos y Calidad Ambiental");
    }

    @Test
    void idiomasConservanMarcadoresYCaenAlCastellano() {
        AndraIdiomaServicio idioma = new AndraIdiomaServicio(new AndraJsonRecursos());
        idioma.andraCargar();
        assertThat(idioma.andraTotalTextos()).isGreaterThan(1500);
        Pattern marca = Pattern.compile("\\{\\d+}");
        for (String clave : idioma.andraClaves()) {
            List<String> esperadas = marca.matcher(clave).results().map(r -> r.group()).sorted().toList();
            for (String l : List.of("en", "qu", "ay")) {
                String t = idioma.andraDirecta(clave, l);
                assertThat(t).as(l + ": " + clave).isNotBlank();
                assertThat(marca.matcher(t).results().map(r -> r.group()).sorted().toList()).as(l + ": " + clave).isEqualTo(esperadas);
            }
        }
        for (String s : List.of("Inicio", "Documentos", "Reservas", "Equipos", "Mis Solicitudes", "Ayuda", "Favoritos", "Panel", "Análisis",
                "Trámites", "Transacciones", "Préstamos", "Notificaciones", "Idioma", "Cerrar sesión", "Ingresa a tu cuenta", "Mi carné digital",
                "Seguimiento en vivo", "Buscar disponibilidad", "Filtros rápidos", "Lo que dicen los datos")) {
            for (String l : List.of("en", "qu", "ay")) {
                assertThat(idioma.andraTraducir(s, l)).as(l + ": " + s).isNotEqualTo(s);
            }
        }
        assertThat(idioma.andraTraducir("Tienes 3 pagos pendientes, incluida tu matrícula 2026-2.", "es")).startsWith("Tienes 3");
        assertThat(idioma.andraTraducir("Se renueva en 7 s", "en")).isEqualTo("Renews in 7 s");
        assertThat(idioma.andraTraducir("Texto inexistente para la prueba", "en")).isEqualTo("Texto inexistente para la prueba");
        assertThat(idioma.andraTraducir("Martes, 20 de octubre, 2026", "en")).isEqualTo("Tuesday, October 20, 2026");
        assertThat(idioma.andraTraducir("Martes, 20 de octubre, 2026", "qu")).endsWith("20 octubre killa, 2026");
        assertThat(idioma.andraTraducir("Martes, 20 de octubre, 2026", "ay")).contains("octubre phaxsi");
    }
}
