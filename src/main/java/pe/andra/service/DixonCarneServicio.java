package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.DixonPago;
import pe.andra.model.DixonPersona;
import pe.andra.model.DixonUsoCarne;
import pe.andra.repository.DixonUsoCarneRepositorio;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonHash;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DixonCarneServicio {

    public static final long DIXON_PASO_MS = 15000;
    public static final List<String> DIXON_LUGARES = List.of("Biblioteca Central · Sede Central",
            "Préstamo de equipos · Pabellón Central", "Laboratorio de Informática A-450", "Auditorio Principal");

    private static final String DIXON_SEMILLA_CJSV = "andra";
    private static final Pattern DIXON_TOKEN = Pattern.compile("^ANDRA-ID\\|(\\w+)\\|(\\d+)\\|(\\w+)$");
    private static final Random DIXON_AZAR = new Random();

    public record DixonVerificacion(boolean dixonCorrecto, String dixonMensaje, DixonPersona dixonPersona) {
    }

    private final DixonSesionServicio dixonSesion;
    private final DixonPagoServicio dixonPagos;
    private final DixonCatalogoServicio dixonCatalogo;
    private final DixonUsoCarneRepositorio dixonUsos;

    public static long dixonFranja(long dixonMilis) {
        return Math.floorDiv(dixonMilis, DIXON_PASO_MS);
    }

    public static String dixonFirma(String dixonCodigo, long dixonFranja) {
        byte[] dixonAlto = DixonHash.dixonSha256(dixonCodigo + "|" + dixonFranja + "|" + DIXON_SEMILLA_CJSV);
        long dixonNumero = 0;
        for (int dixonIndice = 0; dixonIndice < 8; dixonIndice++) {
            dixonNumero = (dixonNumero << 8) | (dixonAlto[dixonIndice] & 0xFF);
        }
        String dixonTexto36 = Long.toUnsignedString(dixonNumero, 36).toUpperCase();
        while (dixonTexto36.length() < 6) {
            dixonTexto36 = "0" + dixonTexto36;
        }
        return dixonTexto36.substring(0, 6);
    }

    public static String dixonToken(String dixonCodigo, long dixonFranja) {
        return "ANDRA-ID|" + dixonCodigo + "|" + dixonFranja + "|" + dixonFirma(dixonCodigo, dixonFranja);
    }

    public String dixonTokenActual() {
        return dixonToken(dixonSesion.dixonEstudiante().getDixonCodigo(), dixonFranja(DixonFechas.dixonMilis()));
    }

    public static long dixonSegundosRestantes(long dixonMilis) {
        long dixonResta = DIXON_PASO_MS - Math.floorMod(dixonMilis, DIXON_PASO_MS);
        return (long) Math.ceil(dixonResta / 1000.0);
    }

    public static double dixonFraccionRestante(long dixonMilis) {
        return (DIXON_PASO_MS - Math.floorMod(dixonMilis, DIXON_PASO_MS)) / (double) DIXON_PASO_MS;
    }

    public DixonVerificacion dixonVerificar(String dixonEntrada) {
        return dixonVerificar(dixonEntrada, dixonFranja(DixonFechas.dixonMilis()));
    }

    @Transactional(readOnly = true)
    public DixonVerificacion dixonVerificar(String dixonEntrada, long dixonFranjaActual) {
        String dixonValor = dixonEntrada == null ? "" : dixonEntrada.trim();
        Matcher dixonCoincidencia = DIXON_TOKEN.matcher(dixonValor);
        if (dixonCoincidencia.matches()) {
            long dixonFranjaToken = Long.parseLong(dixonCoincidencia.group(2));
            if (Math.abs(dixonFranjaActual - dixonFranjaToken) > 1) {
                return new DixonVerificacion(false, "Código vencido. Pide al estudiante que muestre el código actual.", null);
            }
            if (!dixonFirma(dixonCoincidencia.group(1), dixonFranjaToken).equals(dixonCoincidencia.group(3))) {
                return new DixonVerificacion(false, "Firma inválida: el código fue alterado.", null);
            }
            return new DixonVerificacion(true, "Carné válido", dixonCatalogo.dixonPersona(dixonCoincidencia.group(1)).orElse(null));
        }
        Optional<DixonPersona> dixonOpcional = dixonCatalogo.dixonPersonaPorCodigoODni(dixonValor);
        return dixonOpcional.map(dixonPosicionX -> new DixonVerificacion(true, "Estudiante encontrado (validación manual por código)", dixonPosicionX))
                .orElseGet(() -> new DixonVerificacion(false, "No encontramos ese código o DNI.", null));
    }

    public boolean dixonVigente() {
        return dixonPagos.dixonMatricula().dixonEsConfirmada();
    }

    @Transactional
    public DixonUsoCarne dixonSimularLectura() {
        String dixonLugar = DIXON_LUGARES.get(DIXON_AZAR.nextInt(DIXON_LUGARES.size()));
        boolean dixonCorrecto = dixonVerificar(dixonTokenActual()).dixonCorrecto() && dixonVigente();
        return dixonUsos.save(DixonUsoCarne.builder().dixonLugar(dixonLugar).dixonFecha(DixonFechas.dixonAhora()).dixonConcedido(dixonCorrecto).build());
    }

    @Transactional(readOnly = true)
    public List<DixonUsoCarne> dixonUltimos() {
        return dixonUsos.findAllByOrderByDixonFechaDesc().stream().limit(6).toList();
    }

    @Transactional(readOnly = true)
    public Optional<byte[]> dixonFoto() {
        return dixonPagos.dixonMios().stream().map(DixonPago::getDixonFoto).filter(dixonBytes -> dixonBytes != null && dixonBytes.length > 0).findFirst();
    }
}
