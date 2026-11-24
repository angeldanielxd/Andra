package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.AndraPago;
import pe.andra.model.AndraPersona;
import pe.andra.model.AndraUsoCarne;
import pe.andra.repository.AndraUsoCarneRepositorio;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraHash;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AndraCarneServicio {

    public static final long ANDRA_PASO_MS = 15000;
    public static final List<String> ANDRA_LUGARES = List.of("Biblioteca Central · Sede Central",
            "Préstamo de equipos · Pabellón Central", "Laboratorio de Informática A-450", "Auditorio Principal");

    private static final String ANDRA_SEMILLA_CJSV = "andra";
    private static final Pattern ANDRA_TOKEN = Pattern.compile("^ANDRA-ID\\|(\\w+)\\|(\\d+)\\|(\\w+)$");
    private static final Random ANDRA_AZAR = new Random();

    public record AndraVerificacion(boolean andraCorrecto, String andraMensaje, AndraPersona andraPersona) {
    }

    private final AndraSesionServicio andraSesion;
    private final AndraPagoServicio andraPagos;
    private final AndraCatalogoServicio andraCatalogo;
    private final AndraUsoCarneRepositorio andraUsos;

    public static long andraFranja(long andraMilis) {
        return Math.floorDiv(andraMilis, ANDRA_PASO_MS);
    }

    public static String andraFirma(String andraCodigo, long andraFranja) {
        byte[] andraAlto = AndraHash.andraSha256(andraCodigo + "|" + andraFranja + "|" + ANDRA_SEMILLA_CJSV);
        long andraNumero = 0;
        for (int andraIndice = 0; andraIndice < 8; andraIndice++) {
            andraNumero = (andraNumero << 8) | (andraAlto[andraIndice] & 0xFF);
        }
        String andraTexto36 = Long.toUnsignedString(andraNumero, 36).toUpperCase();
        while (andraTexto36.length() < 6) {
            andraTexto36 = "0" + andraTexto36;
        }
        return andraTexto36.substring(0, 6);
    }

    public static String andraToken(String andraCodigo, long andraFranja) {
        return "ANDRA-ID|" + andraCodigo + "|" + andraFranja + "|" + andraFirma(andraCodigo, andraFranja);
    }

    public String andraTokenActual() {
        return andraToken(andraSesion.andraEstudiante().getAndraCodigo(), andraFranja(AndraFechas.andraMilis()));
    }

    public static long andraSegundosRestantes(long andraMilis) {
        long andraResta = ANDRA_PASO_MS - Math.floorMod(andraMilis, ANDRA_PASO_MS);
        return (long) Math.ceil(andraResta / 1000.0);
    }

    public static double andraFraccionRestante(long andraMilis) {
        return (ANDRA_PASO_MS - Math.floorMod(andraMilis, ANDRA_PASO_MS)) / (double) ANDRA_PASO_MS;
    }

    public AndraVerificacion andraVerificar(String andraEntrada) {
        return andraVerificar(andraEntrada, andraFranja(AndraFechas.andraMilis()));
    }

    @Transactional(readOnly = true)
    public AndraVerificacion andraVerificar(String andraEntrada, long andraFranjaActual) {
        String andraValor = andraEntrada == null ? "" : andraEntrada.trim();
        Matcher andraCoincidencia = ANDRA_TOKEN.matcher(andraValor);
        if (andraCoincidencia.matches()) {
            long andraFranjaToken = Long.parseLong(andraCoincidencia.group(2));
            if (Math.abs(andraFranjaActual - andraFranjaToken) > 1) {
                return new AndraVerificacion(false, "Código vencido. Pide al estudiante que muestre el código actual.", null);
            }
            if (!andraFirma(andraCoincidencia.group(1), andraFranjaToken).equals(andraCoincidencia.group(3))) {
                return new AndraVerificacion(false, "Firma inválida: el código fue alterado.", null);
            }
            return new AndraVerificacion(true, "Carné válido", andraCatalogo.andraPersona(andraCoincidencia.group(1)).orElse(null));
        }
        Optional<AndraPersona> andraOpcional = andraCatalogo.andraPersonaPorCodigoODni(andraValor);
        return andraOpcional.map(andraPosicionX -> new AndraVerificacion(true, "Estudiante encontrado (validación manual por código)", andraPosicionX))
                .orElseGet(() -> new AndraVerificacion(false, "No encontramos ese código o DNI.", null));
    }

    public boolean andraVigente() {
        return andraPagos.andraMatricula().andraEsConfirmada();
    }

    @Transactional
    public AndraUsoCarne andraSimularLectura() {
        String andraLugar = ANDRA_LUGARES.get(ANDRA_AZAR.nextInt(ANDRA_LUGARES.size()));
        boolean andraCorrecto = andraVerificar(andraTokenActual()).andraCorrecto() && andraVigente();
        return andraUsos.save(AndraUsoCarne.builder().andraLugar(andraLugar).andraFecha(AndraFechas.andraAhora()).andraConcedido(andraCorrecto).build());
    }

    @Transactional(readOnly = true)
    public List<AndraUsoCarne> andraUltimos() {
        return andraUsos.findAllByOrderByAndraFechaDesc().stream().limit(6).toList();
    }

    @Transactional(readOnly = true)
    public Optional<byte[]> andraFoto() {
        return andraPagos.andraMios().stream().map(AndraPago::getAndraFoto).filter(andraBytes -> andraBytes != null && andraBytes.length > 0).findFirst();
    }
}
