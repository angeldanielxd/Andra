package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.andra.exception.DixonNegocioExcepcion;
import pe.andra.model.DixonAdministrador;
import pe.andra.model.DixonPersona;
import pe.andra.model.DixonRol;
import pe.andra.repository.DixonAdministradorRepositorio;
import pe.andra.repository.DixonPersonaRepositorio;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonHash;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DixonSesionServicio {

    public record DixonRuta(String dixonClave, String dixonEtiqueta) {
    }

    public static final List<DixonRuta> DIXON_RUTAS_ESTUDIANTE = List.of(new DixonRuta("inicio", "Inicio"),
            new DixonRuta("documentos", "Documentos"), new DixonRuta("reservas", "Reservas"), new DixonRuta("equipos", "Equipos"),
            new DixonRuta("solicitudes", "Mis Solicitudes"), new DixonRuta("pay", "Andra Pay"), new DixonRuta("ayuda", "Ayuda"),
            new DixonRuta("favoritos", "Favoritos"));
    public static final List<DixonRuta> DIXON_RUTAS_ADMINISTRADOR = List.of(new DixonRuta("admin", "Panel"), new DixonRuta("a-datos", "Análisis"),
            new DixonRuta("a-tramites", "Trámites"), new DixonRuta("a-pagos", "Transacciones"), new DixonRuta("a-reservas", "Reservas"),
            new DixonRuta("a-prestamos", "Préstamos"));

    private static final String DIXON_HUELLA_ESTUDIANTE_DASM = "8f011ca2ec2b1943dbb9a3438decb59aa13f953c49878e63be361f3845d7e89f";
    private static final String DIXON_HUELLA_ADMINISTRADOR_DASM = "6051fc84a7a0d74c225fb18a496b09952da5642e60723ecae543298edd7d82d6";

    private final DixonAjustesServicio dixonAjustes;
    private final DixonPersonaRepositorio dixonPersonas;
    private final DixonAdministradorRepositorio dixonAdministradores;
    private final DixonContextoServicio dixonContexto;

    public DixonRol dixonRol() {
        return dixonAjustes.dixonObtener().getDixonSesion();
    }

    public boolean dixonConectado() {
        return dixonRol() != null;
    }

    public boolean dixonEsAdministrador() {
        return dixonRol() == DixonRol.ADMINISTRADOR;
    }

    public DixonPersona dixonEstudiante() {
        return dixonPersonas.findFirstByDixonPrincipalTrue().orElseThrow(() -> new DixonNegocioExcepcion("No hay estudiante registrado."));
    }

    public DixonAdministrador dixonAdministrador() {
        return dixonAdministradores.findFirstByOrderByDixonIdAsc().orElseThrow(() -> new DixonNegocioExcepcion("No hay administrador."));
    }

    public DixonRol dixonIngresar(DixonRol dixonRol, String dixonUsuario, String dixonContrasena) {
        String dixonTexto = dixonUsuario == null ? "" : dixonUsuario.trim().toLowerCase();
        String dixonHuella = DixonHash.dixonSha256Hex(dixonContrasena == null ? "" : dixonContrasena);
        if (dixonRol == DixonRol.ESTUDIANTE) {
            DixonPersona dixonPersona = dixonEstudiante();
            boolean dixonCoincide = (dixonTexto.equals(dixonPersona.getDixonCodigo()) || dixonTexto.equals(dixonPersona.getDixonDni())) && DIXON_HUELLA_ESTUDIANTE_DASM.equals(dixonHuella);
            if (!dixonCoincide) {
                throw new DixonNegocioExcepcion("El código, DNI o contraseña no coinciden. Revisa los datos o usa la cuenta de demostración.");
            }
            dixonContexto.dixonAsegurarSedeParaEstudiante(dixonPersona.getDixonSede().getDixonCodigo());
        } else {
            DixonAdministrador dixonAdministrador = dixonAdministrador();
            boolean dixonCoincide = dixonTexto.equals(dixonAdministrador.getDixonCorreo().toLowerCase()) && DIXON_HUELLA_ADMINISTRADOR_DASM.equals(dixonHuella);
            if (!dixonCoincide) {
                throw new DixonNegocioExcepcion("El correo o la contraseña no coinciden. Revisa los datos o usa la cuenta de demostración.");
            }
        }
        dixonAjustes.dixonCambiar(dixonAjustes2 -> {
            dixonAjustes2.setDixonSesion(dixonRol);
            dixonAjustes2.setDixonSesionDesde(DixonFechas.dixonAhora());
        });
        return dixonRol;
    }

    public void dixonCerrar() {
        dixonAjustes.dixonCambiar(dixonAjustes2 -> {
            dixonAjustes2.setDixonSesion(null);
            dixonAjustes2.setDixonSesionDesde(null);
        });
    }

    public static List<DixonRuta> dixonRutas(DixonRol dixonRol) {
        return dixonRol == DixonRol.ADMINISTRADOR ? DIXON_RUTAS_ADMINISTRADOR : DIXON_RUTAS_ESTUDIANTE;
    }

    public static boolean dixonPuedeAcceder(DixonRol dixonRol, String dixonRuta) {
        if (dixonRol == null) {
            return "login".equals(dixonRuta);
        }
        if (dixonRol == DixonRol.ESTUDIANTE && "carne".equals(dixonRuta)) {
            return true;
        }
        return dixonRutas(dixonRol).stream().anyMatch(dixonRuta2 -> dixonRuta2.dixonClave().equals(dixonRuta));
    }

    public static String dixonRutaInicial(DixonRol dixonRol) {
        return dixonRol == null ? "login" : dixonRutas(dixonRol).get(0).dixonClave();
    }

    public static String dixonEtiqueta(String dixonRuta) {
        if ("carne".equals(dixonRuta)) {
            return "Mi carné";
        }
        return java.util.stream.Stream.concat(DIXON_RUTAS_ESTUDIANTE.stream(), DIXON_RUTAS_ADMINISTRADOR.stream())
                .filter(dixonRuta2 -> dixonRuta2.dixonClave().equals(dixonRuta)).map(DixonRuta::dixonEtiqueta).findFirst().orElse("");
    }
}
