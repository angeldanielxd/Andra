package pe.andra.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.config.AndraJsonRecursos;
import pe.andra.model.*;
import pe.andra.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DixonCatalogoSembrador {

    private final AndraJsonRecursos dixonJson;
    private final DixonSedeRepositorio dixonSedes;
    private final DixonEscuelaProfesionalRepositorio dixonEscuelas;
    private final DixonEspacioRepositorio dixonEspacios;
    private final DixonDocumentoRepositorio dixonDocumentos;
    private final DixonCategoriaEquipoRepositorio dixonCategorias;
    private final DixonEquipoRepositorio dixonEquipos;
    private final DixonTramiteRepositorio dixonTramites;
    private final DixonPersonaRepositorio dixonPersonas;
    private final DixonAdministradorRepositorio dixonAdministradores;

    @Transactional
    public int dixonSembrar() {
        JsonNode dixonNodo = dixonJson.andraCatalogo();
        int dixonNuevos = 0;
        for (JsonNode dixonNodo2 : dixonNodo.path("sedes")) {
            if (dixonSedes.findByDixonCodigo(dixonNodo2.path("id").asText()).isEmpty()) {
                dixonSedes.save(DixonSede.builder().dixonCodigo(dixonNodo2.path("id").asText()).dixonNombre(dixonNodo2.path("name").asText())
                        .dixonNombreCompleto(dixonNodo2.path("full").asText()).dixonDescripcion(dixonNodo2.path("desc").asText())
                        .dixonOficina(dixonNodo2.path("office").asText()).dixonHorario(dixonNodo2.path("hours").asText()).build());
                dixonNuevos++;
            }
        }
        for (JsonNode dixonNodo3 : dixonNodo.path("escuelas")) {
            if (dixonEscuelas.findByDixonCodigo(dixonNodo3.path("id").asText()).isEmpty()) {
                dixonEscuelas.save(DixonEscuelaProfesional.builder().dixonCodigo(dixonNodo3.path("id").asText()).dixonNombre(dixonNodo3.path("name").asText())
                        .dixonSede(dixonSede(dixonNodo3.path("sede").asText())).build());
                dixonNuevos++;
            }
        }
        for (JsonNode dixonNodo4 : dixonNodo.path("espacios")) {
            if (dixonEspacios.findByDixonCodigo(dixonNodo4.path("id").asText()).isEmpty()) {
                dixonEspacios.save(DixonEspacio.builder().dixonCodigo(dixonNodo4.path("id").asText()).dixonNombre(dixonNodo4.path("name").asText())
                        .dixonTipo(DixonTipoEspacio.dixonDesdeEtiqueta(dixonNodo4.path("type").asText())).dixonCapacidad(dixonNodo4.path("cap").asInt())
                        .dixonFueraDeServicio("Ocupado".equals(dixonNodo4.path("status").asText()))
                        .dixonEdificio(dixonNodo4.path("bld").asText()).dixonSede(dixonSede(dixonNodo4.path("sede").asText()))
                        .dixonEscuela(dixonEscuela(dixonNodo4.path("fac").asText(null))).build());
                dixonNuevos++;
            }
        }
        for (JsonNode dixonNodo5 : dixonNodo.path("documentos")) {
            if (dixonDocumentos.findByDixonCodigo(dixonNodo5.path("id").asText()).isEmpty()) {
                dixonDocumentos.save(DixonDocumento.builder().dixonCodigo(dixonNodo5.path("id").asText()).dixonNombre(dixonNodo5.path("name").asText())
                        .dixonDescripcion(dixonNodo5.path("desc").asText()).dixonTipo(dixonNodo5.path("type").asText())
                        .dixonDependencia(dixonNodo5.path("dep").asText()).dixonPublico(dixonNodo5.path("aud").asText())
                        .dixonFecha(LocalDate.parse(dixonNodo5.path("date").asText())).dixonFrecuente(dixonNodo5.path("freq").asBoolean())
                        .dixonPaginas(dixonNodo5.path("pages").asInt(1)).dixonEscuela(dixonEscuela(dixonNodo5.path("fac").asText(null))).build());
                dixonNuevos++;
            }
        }
        for (JsonNode dixonNodo6 : dixonNodo.path("categoriasEquipo")) {
            if (dixonCategorias.findByDixonCodigo(dixonNodo6.path("id").asText()).isEmpty()) {
                dixonCategorias.save(DixonCategoriaEquipo.builder().dixonCodigo(dixonNodo6.path("id").asText()).dixonNombre(dixonNodo6.path("name").asText())
                        .dixonDescripcion(dixonNodo6.path("desc").asText()).dixonIcono(dixonNodo6.path("icon").asText()).build());
                dixonNuevos++;
            }
        }
        for (JsonNode dixonNodo7 : dixonNodo.path("equipos")) {
            if (dixonEquipos.findByDixonCodigo(dixonNodo7.path("id").asText()).isEmpty()) {
                dixonEquipos.save(DixonEquipo.builder().dixonCodigo(dixonNodo7.path("id").asText())
                        .dixonCategoria(dixonCategorias.findByDixonCodigo(dixonNodo7.path("cat").asText()).orElseThrow())
                        .dixonRotulo(dixonNodo7.path("label").asText()).dixonNombre(dixonNodo7.path("name").asText())
                        .dixonEspecificaciones(dixonNodo7.path("specs").asText()).dixonUbicacion(dixonNodo7.path("loc").asText())
                        .dixonPuntoEntrega(dixonNodo7.path("point").asText()).dixonSede(dixonSede(dixonNodo7.path("sede").asText()))
                        .dixonEscuela(dixonEscuela(dixonNodo7.path("fac").asText(null))).dixonUnidadesIniciales(dixonNodo7.path("units").asInt())
                        .dixonSaldo(dixonNodo7.path("units").asInt()).dixonUnidad(dixonNodo7.path("unit").asText())
                        .dixonModo("turno".equals(dixonNodo7.path("mode").asText()) ? DixonModoEquipo.TURNO : DixonModoEquipo.PRESTAMO)
                        .dixonProximoTurno(dixonNodo7.path("next").asText(null)).build());
                dixonNuevos++;
            }
        }
        for (JsonNode dixonNodo8 : dixonNodo.path("tarifario")) {
            if (dixonTramites.findByDixonCodigo(dixonNodo8.path("id").asText()).isEmpty()) {
                List<String> dixonAud = new ArrayList<>();
                dixonNodo8.path("aud").forEach(dixonNodo9 -> dixonAud.add(dixonNodo9.asText()));
                dixonTramites.save(DixonTramite.builder().dixonCodigo(dixonNodo8.path("id").asText()).dixonNombre(dixonNodo8.path("name").asText())
                        .dixonCategoria(dixonNodo8.path("cat").asText()).dixonMonto(new BigDecimal(dixonNodo8.path("monto").asText()))
                        .dixonEntrega(dixonNodo8.path("entrega").asText()).dixonPublicos(String.join("|", dixonAud))
                        .dixonPlantilla(dixonNodo8.path("tpl").isNull() ? null : dixonNodo8.path("tpl").asText(null))
                        .dixonDescripcion(dixonNodo8.path("desc").asText()).dixonUnidad(dixonNodo8.path("unidad").asText(null))
                        .dixonPorCantidad(dixonNodo8.path("qty").asBoolean(false)).build());
                dixonNuevos++;
            }
        }
        JsonNode dixonNodo10 = dixonJson.andraDemo().path("usuario");
        if (dixonPersonas.findByDixonCodigo(dixonNodo10.path("codigo").asText()).isEmpty()) {
            dixonPersonas.save(DixonPersona.builder().dixonCodigo(dixonNodo10.path("codigo").asText()).dixonNombre(dixonNodo10.path("nombre").asText())
                    .dixonDni(dixonNodo10.path("dni").asText()).dixonTipo("Estudiante").dixonEscuela(dixonEscuela(dixonNodo10.path("escuela").asText()))
                    .dixonSede(dixonSede(dixonNodo10.path("sede").asText())).dixonCorreo(dixonNodo10.path("correo").asText())
                    .dixonCelular(dixonNodo10.path("celular").asText()).dixonPrincipal(true).build());
            dixonNuevos++;
        }
        for (JsonNode dixonNodo11 : dixonNodo.path("usuarios")) {
            String dixonCodigo = dixonNodo11.path("code").asText();
            if (dixonPersonas.findByDixonCodigo(dixonCodigo).isEmpty()) {
                dixonPersonas.save(DixonPersona.builder().dixonCodigo(dixonCodigo).dixonNombre(dixonNodo11.path("name").asText())
                        .dixonDni(dixonNodo11.path("dni").asText()).dixonTipo(dixonNodo11.path("tipo").asText())
                        .dixonEscuela(dixonEscuela(dixonNodo11.path("fac").asText(null))).dixonSede(dixonSede(dixonNodo11.path("sede").asText()))
                        .dixonCorreo(dixonCodigo.toLowerCase() + "@institucion.edu").dixonPrincipal(false).build());
                dixonNuevos++;
            }
        }
        JsonNode dixonNodo12 = dixonJson.andraDemo().path("cuentasDemo").path("administrador");
        if (dixonAdministradores.findByDixonCorreo(dixonNodo12.path("correo").asText()).isEmpty()) {
            dixonAdministradores.save(DixonAdministrador.builder().dixonNombre(dixonNodo12.path("nombre").asText())
                    .dixonCorreo(dixonNodo12.path("correo").asText()).dixonCargo(dixonNodo12.path("cargo").asText()).dixonDni("41236587").build());
            dixonNuevos++;
        }
        return dixonNuevos;
    }

    private DixonSede dixonSede(String dixonCodigo) {
        return dixonSedes.findByDixonCodigo(dixonCodigo).orElseThrow(() -> new IllegalStateException("Sede " + dixonCodigo));
    }

    private DixonEscuelaProfesional dixonEscuela(String dixonCodigo) {
        if (dixonCodigo == null || dixonCodigo.isBlank() || "null".equals(dixonCodigo)) {
            return null;
        }
        return dixonEscuelas.findByDixonCodigo(dixonCodigo).orElse(null);
    }
}
