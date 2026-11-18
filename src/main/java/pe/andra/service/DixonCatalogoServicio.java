package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.config.AndraJsonRecursos;
import pe.andra.model.*;
import pe.andra.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DixonCatalogoServicio {

    public static final List<String> DIXON_FRANJAS = List.of("08:00", "10:00", "12:00", "14:00", "16:00");
    public static final List<String> DIXON_HORARIOS = List.of("08:00 - 10:00", "10:00 - 12:00", "12:00 - 14:00",
            "14:00 - 16:00", "16:00 - 18:00");
    public static final List<String> DIXON_SOLICITANTES = List.of("Estudiante", "Egresado", "Docente", "Administrativo",
            "Público en general");

    private final DixonSedeRepositorio dixonSedes;
    private final DixonEscuelaProfesionalRepositorio dixonEscuelas;
    private final DixonEspacioRepositorio dixonEspacios;
    private final DixonDocumentoRepositorio dixonDocumentos;
    private final DixonCategoriaEquipoRepositorio dixonCategorias;
    private final DixonEquipoRepositorio dixonEquipos;
    private final DixonTramiteRepositorio dixonTramites;
    private final DixonPersonaRepositorio dixonPersonas;
    private final AndraJsonRecursos dixonJson;

    public List<DixonSede> dixonSedes() {
        return dixonSedes.findAllByOrderByDixonIdAsc();
    }

    public Optional<DixonSede> dixonSede(String dixonCodigo) {
        return dixonSedes.findByDixonCodigo(dixonCodigo);
    }

    public List<DixonEscuelaProfesional> dixonEscuelas() {
        return dixonEscuelas.findAllByOrderByDixonIdAsc();
    }

    public List<DixonEscuelaProfesional> dixonEscuelasDe(DixonSede dixonSede) {
        return dixonEscuelas().stream().filter(dixonEscuelaProfesional -> dixonEscuelaProfesional.getDixonSede().getDixonId().equals(dixonSede.getDixonId())).toList();
    }

    public Optional<DixonEscuelaProfesional> dixonEscuela(String dixonCodigo) {
        return dixonCodigo == null ? Optional.empty() : dixonEscuelas.findByDixonCodigo(dixonCodigo);
    }

    public String dixonNombreEscuela(String dixonCodigo) {
        return dixonEscuela(dixonCodigo).map(DixonEscuelaProfesional::getDixonNombre).orElse("Uso general");
    }

    public List<DixonEspacio> dixonEspacios() {
        return dixonEspacios.findAllByOrderByDixonIdAsc();
    }

    public Optional<DixonEspacio> dixonEspacio(String dixonCodigo) {
        return dixonEspacios.findByDixonCodigo(dixonCodigo);
    }

    public List<DixonDocumento> dixonDocumentos() {
        return dixonDocumentos.findAllByOrderByDixonIdAsc();
    }

    public List<DixonCategoriaEquipo> dixonCategorias() {
        return dixonCategorias.findAllByOrderByDixonIdAsc();
    }

    public List<DixonEquipo> dixonEquipos() {
        return dixonEquipos.findAllByOrderByDixonIdAsc();
    }

    public Optional<DixonEquipo> dixonEquipo(String dixonCodigo) {
        return dixonEquipos.findByDixonCodigo(dixonCodigo);
    }

    public List<DixonTramite> dixonTramites() {
        return dixonTramites.findAllByOrderByDixonIdAsc();
    }

    public Optional<DixonTramite> dixonTramite(String dixonCodigo) {
        return dixonTramites.findByDixonCodigo(dixonCodigo);
    }

    public List<DixonPersona> dixonPersonas() {
        return dixonPersonas.findAllByOrderByDixonIdAsc();
    }

    public Optional<DixonPersona> dixonPersona(String dixonCodigo) {
        return dixonPersonas.findByDixonCodigo(dixonCodigo);
    }

    public Optional<DixonPersona> dixonPersonaPorCodigoODni(String dixonValor) {
        String dixonValor2 = dixonValor == null ? "" : dixonValor.trim();
        return dixonPersonas.findByDixonCodigo(dixonValor2).or(() -> dixonPersonas.findFirstByDixonDni(dixonValor2));
    }

    public List<String> dixonTiposDocumento() {
        List<String> dixonLista = new ArrayList<>();
        dixonJson.andraCatalogo().path("tiposDocumento").forEach(dixonNodo -> dixonLista.add(dixonNodo.asText()));
        return dixonLista;
    }

    public List<String> dixonDependencias() {
        List<String> dixonLista = new ArrayList<>();
        dixonJson.andraCatalogo().path("dependenciasAdministrativas").forEach(dixonNodo -> dixonLista.add(dixonNodo.asText()));
        return dixonLista;
    }

    public String dixonEtiquetaDependencia(String dixonDependencia) {
        return dixonEscuelas().stream().anyMatch(dixonEscuelaProfesional -> dixonEscuelaProfesional.getDixonNombre().equals(dixonDependencia))
                ? "Escuela Profesional de " + dixonDependencia : dixonDependencia;
    }
}
