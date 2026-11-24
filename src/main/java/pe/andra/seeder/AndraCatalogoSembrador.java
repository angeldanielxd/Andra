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
public class AndraCatalogoSembrador {

    private final AndraJsonRecursos andraJson;
    private final AndraSedeRepositorio andraSedes;
    private final AndraEscuelaProfesionalRepositorio andraEscuelas;
    private final AndraEspacioRepositorio andraEspacios;
    private final AndraDocumentoRepositorio andraDocumentos;
    private final AndraCategoriaEquipoRepositorio andraCategorias;
    private final AndraEquipoRepositorio andraEquipos;
    private final AndraTramiteRepositorio andraTramites;
    private final AndraPersonaRepositorio andraPersonas;
    private final AndraAdministradorRepositorio andraAdministradores;

    @Transactional
    public int andraSembrar() {
        JsonNode andraNodo = andraJson.andraCatalogo();
        int andraNuevos = 0;
        for (JsonNode andraNodo2 : andraNodo.path("sedes")) {
            if (andraSedes.findByAndraCodigo(andraNodo2.path("id").asText()).isEmpty()) {
                andraSedes.save(AndraSede.builder().andraCodigo(andraNodo2.path("id").asText()).andraNombre(andraNodo2.path("name").asText())
                        .andraNombreCompleto(andraNodo2.path("full").asText()).andraDescripcion(andraNodo2.path("desc").asText())
                        .andraOficina(andraNodo2.path("office").asText()).andraHorario(andraNodo2.path("hours").asText()).build());
                andraNuevos++;
            }
        }
        for (JsonNode andraNodo3 : andraNodo.path("escuelas")) {
            if (andraEscuelas.findByAndraCodigo(andraNodo3.path("id").asText()).isEmpty()) {
                andraEscuelas.save(AndraEscuelaProfesional.builder().andraCodigo(andraNodo3.path("id").asText()).andraNombre(andraNodo3.path("name").asText())
                        .andraSede(andraSede(andraNodo3.path("sede").asText())).build());
                andraNuevos++;
            }
        }
        for (JsonNode andraNodo4 : andraNodo.path("espacios")) {
            if (andraEspacios.findByAndraCodigo(andraNodo4.path("id").asText()).isEmpty()) {
                andraEspacios.save(AndraEspacio.builder().andraCodigo(andraNodo4.path("id").asText()).andraNombre(andraNodo4.path("name").asText())
                        .andraTipo(AndraTipoEspacio.andraDesdeEtiqueta(andraNodo4.path("type").asText())).andraCapacidad(andraNodo4.path("cap").asInt())
                        .andraFueraDeServicio("Ocupado".equals(andraNodo4.path("status").asText()))
                        .andraEdificio(andraNodo4.path("bld").asText()).andraSede(andraSede(andraNodo4.path("sede").asText()))
                        .andraEscuela(andraEscuela(andraNodo4.path("fac").asText(null))).build());
                andraNuevos++;
            }
        }
        for (JsonNode andraNodo5 : andraNodo.path("documentos")) {
            if (andraDocumentos.findByAndraCodigo(andraNodo5.path("id").asText()).isEmpty()) {
                andraDocumentos.save(AndraDocumento.builder().andraCodigo(andraNodo5.path("id").asText()).andraNombre(andraNodo5.path("name").asText())
                        .andraDescripcion(andraNodo5.path("desc").asText()).andraTipo(andraNodo5.path("type").asText())
                        .andraDependencia(andraNodo5.path("dep").asText()).andraPublico(andraNodo5.path("aud").asText())
                        .andraFecha(LocalDate.parse(andraNodo5.path("date").asText())).andraFrecuente(andraNodo5.path("freq").asBoolean())
                        .andraPaginas(andraNodo5.path("pages").asInt(1)).andraEscuela(andraEscuela(andraNodo5.path("fac").asText(null))).build());
                andraNuevos++;
            }
        }
        for (JsonNode andraNodo6 : andraNodo.path("categoriasEquipo")) {
            if (andraCategorias.findByAndraCodigo(andraNodo6.path("id").asText()).isEmpty()) {
                andraCategorias.save(AndraCategoriaEquipo.builder().andraCodigo(andraNodo6.path("id").asText()).andraNombre(andraNodo6.path("name").asText())
                        .andraDescripcion(andraNodo6.path("desc").asText()).andraIcono(andraNodo6.path("icon").asText()).build());
                andraNuevos++;
            }
        }
        for (JsonNode andraNodo7 : andraNodo.path("equipos")) {
            if (andraEquipos.findByAndraCodigo(andraNodo7.path("id").asText()).isEmpty()) {
                andraEquipos.save(AndraEquipo.builder().andraCodigo(andraNodo7.path("id").asText())
                        .andraCategoria(andraCategorias.findByAndraCodigo(andraNodo7.path("cat").asText()).orElseThrow())
                        .andraRotulo(andraNodo7.path("label").asText()).andraNombre(andraNodo7.path("name").asText())
                        .andraEspecificaciones(andraNodo7.path("specs").asText()).andraUbicacion(andraNodo7.path("loc").asText())
                        .andraPuntoEntrega(andraNodo7.path("point").asText()).andraSede(andraSede(andraNodo7.path("sede").asText()))
                        .andraEscuela(andraEscuela(andraNodo7.path("fac").asText(null))).andraUnidadesIniciales(andraNodo7.path("units").asInt())
                        .andraSaldo(andraNodo7.path("units").asInt()).andraUnidad(andraNodo7.path("unit").asText())
                        .andraModo("turno".equals(andraNodo7.path("mode").asText()) ? AndraModoEquipo.TURNO : AndraModoEquipo.PRESTAMO)
                        .andraProximoTurno(andraNodo7.path("next").asText(null)).build());
                andraNuevos++;
            }
        }
        for (JsonNode andraNodo8 : andraNodo.path("tarifario")) {
            if (andraTramites.findByAndraCodigo(andraNodo8.path("id").asText()).isEmpty()) {
                List<String> andraAud = new ArrayList<>();
                andraNodo8.path("aud").forEach(andraNodo9 -> andraAud.add(andraNodo9.asText()));
                andraTramites.save(AndraTramite.builder().andraCodigo(andraNodo8.path("id").asText()).andraNombre(andraNodo8.path("name").asText())
                        .andraCategoria(andraNodo8.path("cat").asText()).andraMonto(new BigDecimal(andraNodo8.path("monto").asText()))
                        .andraEntrega(andraNodo8.path("entrega").asText()).andraPublicos(String.join("|", andraAud))
                        .andraPlantilla(andraNodo8.path("tpl").isNull() ? null : andraNodo8.path("tpl").asText(null))
                        .andraDescripcion(andraNodo8.path("desc").asText()).andraUnidad(andraNodo8.path("unidad").asText(null))
                        .andraPorCantidad(andraNodo8.path("qty").asBoolean(false)).build());
                andraNuevos++;
            }
        }
        JsonNode andraNodo10 = andraJson.andraDemo().path("usuario");
        if (andraPersonas.findByAndraCodigo(andraNodo10.path("codigo").asText()).isEmpty()) {
            andraPersonas.save(AndraPersona.builder().andraCodigo(andraNodo10.path("codigo").asText()).andraNombre(andraNodo10.path("nombre").asText())
                    .andraDni(andraNodo10.path("dni").asText()).andraTipo("Estudiante").andraEscuela(andraEscuela(andraNodo10.path("escuela").asText()))
                    .andraSede(andraSede(andraNodo10.path("sede").asText())).andraCorreo(andraNodo10.path("correo").asText())
                    .andraCelular(andraNodo10.path("celular").asText()).andraPrincipal(true).build());
            andraNuevos++;
        }
        for (JsonNode andraNodo11 : andraNodo.path("usuarios")) {
            String andraCodigo = andraNodo11.path("code").asText();
            if (andraPersonas.findByAndraCodigo(andraCodigo).isEmpty()) {
                andraPersonas.save(AndraPersona.builder().andraCodigo(andraCodigo).andraNombre(andraNodo11.path("name").asText())
                        .andraDni(andraNodo11.path("dni").asText()).andraTipo(andraNodo11.path("tipo").asText())
                        .andraEscuela(andraEscuela(andraNodo11.path("fac").asText(null))).andraSede(andraSede(andraNodo11.path("sede").asText()))
                        .andraCorreo(andraCodigo.toLowerCase() + "@institucion.edu").andraPrincipal(false).build());
                andraNuevos++;
            }
        }
        JsonNode andraNodo12 = andraJson.andraDemo().path("cuentasDemo").path("administrador");
        if (andraAdministradores.findByAndraCorreo(andraNodo12.path("correo").asText()).isEmpty()) {
            andraAdministradores.save(AndraAdministrador.builder().andraNombre(andraNodo12.path("nombre").asText())
                    .andraCorreo(andraNodo12.path("correo").asText()).andraCargo(andraNodo12.path("cargo").asText()).andraDni("41236587").build());
            andraNuevos++;
        }
        return andraNuevos;
    }

    private AndraSede andraSede(String andraCodigo) {
        return andraSedes.findByAndraCodigo(andraCodigo).orElseThrow(() -> new IllegalStateException("Sede " + andraCodigo));
    }

    private AndraEscuelaProfesional andraEscuela(String andraCodigo) {
        if (andraCodigo == null || andraCodigo.isBlank() || "null".equals(andraCodigo)) {
            return null;
        }
        return andraEscuelas.findByAndraCodigo(andraCodigo).orElse(null);
    }
}
