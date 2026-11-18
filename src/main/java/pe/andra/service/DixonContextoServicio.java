package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.DixonAjustes;
import pe.andra.model.DixonEscuelaProfesional;
import pe.andra.model.DixonSede;

@Service
@RequiredArgsConstructor
public class DixonContextoServicio {

    private final DixonAjustesServicio dixonAjustes;
    private final DixonCatalogoServicio dixonCatalogo;

    public boolean dixonTodasLasSedes() {
        return DixonAjustes.DIXON_TODAS.equals(dixonAjustes.dixonObtener().getDixonSede());
    }

    public String dixonCodigoSede() {
        return dixonAjustes.dixonObtener().getDixonSede();
    }

    public DixonSede dixonSede() {
        String dixonTexto = dixonCodigoSede();
        return DixonAjustes.DIXON_TODAS.equals(dixonTexto) ? null : dixonCatalogo.dixonSede(dixonTexto).orElse(null);
    }

    public DixonSede dixonSedeOPrincipal() {
        DixonSede dixonSede = dixonSede();
        return dixonSede != null ? dixonSede : dixonCatalogo.dixonSede("central").orElseThrow();
    }

    public String dixonNombreSede() {
        DixonSede dixonSede = dixonSede();
        return dixonSede == null ? "Todas" : dixonSede.getDixonNombre();
    }

    public String dixonTextoSede() {
        DixonSede dixonSede = dixonSede();
        return dixonSede == null ? "Todas las sedes" : "Sede " + dixonSede.getDixonNombre();
    }

    public DixonEscuelaProfesional dixonEscuela() {
        return dixonCatalogo.dixonEscuela(dixonAjustes.dixonObtener().getDixonEscuela()).orElse(null);
    }

    public String dixonTextoEscuela() {
        DixonEscuelaProfesional dixonEscuelaProfesional = dixonEscuela();
        return dixonEscuelaProfesional == null ? "Todas las escuelas" : "EP " + dixonEscuelaProfesional.getDixonNombre();
    }

    public boolean dixonIncluyeSede(DixonSede dixonSede) {
        String dixonTexto = dixonCodigoSede();
        return DixonAjustes.DIXON_TODAS.equals(dixonTexto) || (dixonSede != null && dixonSede.getDixonCodigo().equals(dixonTexto));
    }

    public boolean dixonIncluyeEscuela(DixonEscuelaProfesional dixonEscuelaRecurso) {
        String dixonElegida = dixonAjustes.dixonObtener().getDixonEscuela();
        return dixonElegida == null || dixonEscuelaRecurso == null || dixonEscuelaRecurso.getDixonCodigo().equals(dixonElegida);
    }

    public boolean dixonIncluye(DixonSede dixonSede, DixonEscuelaProfesional dixonEscuelaRecurso) {
        return dixonIncluyeSede(dixonSede) && dixonIncluyeEscuela(dixonEscuelaRecurso);
    }

    public static boolean dixonPertenece(String dixonSedeContexto, String dixonEscuelaContexto, DixonSede dixonSede, DixonEscuelaProfesional dixonEscuela) {
        boolean dixonEnSede = DixonAjustes.DIXON_TODAS.equals(dixonSedeContexto) || (dixonSede != null && dixonSede.getDixonCodigo().equals(dixonSedeContexto));
        boolean dixonEnEscuela = dixonEscuelaContexto == null || dixonEscuela == null || dixonEscuela.getDixonCodigo().equals(dixonEscuelaContexto);
        return dixonEnSede && dixonEnEscuela;
    }

    public String dixonCambiarSede(String dixonCodigo) {
        if (DixonAjustes.DIXON_TODAS.equals(dixonCodigo)) {
            dixonAjustes.dixonCambiar(dixonAjustes2 -> {
                dixonAjustes2.setDixonSede(DixonAjustes.DIXON_TODAS);
                dixonAjustes2.setDixonEscuela(null);
            });
            return "Mostrando información de todas las sedes.";
        }
        DixonSede dixonSede = dixonCatalogo.dixonSede(dixonCodigo).orElseThrow(() -> new DixonNegocioExcepcion("La sede no existe."));
        DixonEscuelaProfesional dixonActual = dixonEscuela();
        boolean dixonLimpia = dixonActual != null && !dixonActual.getDixonSede().getDixonCodigo().equals(dixonCodigo);
        dixonAjustes.dixonCambiar(dixonAjustes3 -> {
            dixonAjustes3.setDixonSede(dixonCodigo);
            if (dixonLimpia) {
                dixonAjustes3.setDixonEscuela(null);
            }
        });
        return "Mostrando información de la Sede " + dixonSede.getDixonNombre() + (dixonLimpia ? " · todas las escuelas" : "") + ".";
    }

    public String dixonCambiarEscuela(String dixonCodigo) {
        if (dixonCodigo == null) {
            dixonAjustes.dixonCambiar(dixonAjustes2 -> dixonAjustes2.setDixonEscuela(null));
            return "Mostrando todas las escuelas de la sede.";
        }
        DixonEscuelaProfesional dixonEscuelaProfesional = dixonCatalogo.dixonEscuela(dixonCodigo).orElseThrow(() -> new DixonNegocioExcepcion("La escuela no existe."));
        dixonAjustes.dixonCambiar(dixonAjustes3 -> {
            dixonAjustes3.setDixonSede(dixonEscuelaProfesional.getDixonSede().getDixonCodigo());
            dixonAjustes3.setDixonEscuela(dixonEscuelaProfesional.getDixonCodigo());
        });
        return "EP " + dixonEscuelaProfesional.getDixonNombre() + " · Sede " + dixonEscuelaProfesional.getDixonSede().getDixonNombre() + ".";
    }

    public void dixonAsegurarSedeParaEstudiante(String dixonSedePropia) {
        if (dixonTodasLasSedes()) {
            dixonAjustes.dixonCambiar(dixonAjustes2 -> {
                dixonAjustes2.setDixonSede(dixonSedePropia);
                dixonAjustes2.setDixonEscuela(null);
            });
        }
    }
}
