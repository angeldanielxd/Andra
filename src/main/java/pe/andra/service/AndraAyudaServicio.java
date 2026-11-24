package pe.andra.service;

import org.springframework.stereotype.Service;
import pe.andra.util.AndraTextoPlano;

import java.util.List;

@Service
public class AndraAyudaServicio {

    public record AndraArea(String andraIcono, String andraEtiqueta, String andraTitulo, String andraDescripcion, List<String> andraPuntos, String andraEnlace) {
        String andraTexto() {
            return andraEtiqueta + " " + andraTitulo + " " + andraDescripcion + " " + String.join(" ", andraPuntos) + " " + andraEnlace;
        }
    }

    public record AndraGuia(String andraPaso, String andraTitulo, List<String> andraPasos, String andraEnlace, String andraDestino) {
        String andraTexto() {
            return andraPaso + " " + andraTitulo + " " + String.join(" ", andraPasos) + " " + andraEnlace + " " + andraDestino;
        }
    }

    public record AndraPregunta(String andraCategoria, String andraPregunta, String andraRespuesta) {
        String andraTexto() {
            return andraCategoria + " " + andraPregunta + " " + andraRespuesta;
        }
    }

    public static final List<AndraArea> ANDRA_AREAS = List.of(
            new AndraArea("fth-file-text", "Trámites académicos", "Gestión de Expedientes y Mesa de Partes",
                    "Seguimiento de estados, plazos de atención y subsanación de carpetas observadas.",
                    List.of("Subsanación de observaciones dentro del plazo de 36 horas útiles.",
                            "Requisitos para constancias de egresado y certificaciones oficiales."),
                    "Ver procedimientos de expedientes"),
            new AndraArea("fth-shield", "Validez y legalidad", "Firma Digital y Certificados SUNEDU",
                    "Validación de documentos electrónicos con código QR y token acreditado de secretaría.",
                    List.of("Verificación de código QR y firma digital del Secretario General.",
                            "Trámite para apostillado ministerial y legalizaciones internacionales."),
                    "Consultar validez y firma electrónica"),
            new AndraArea("fth-calendar", "Infraestructura", "Reservas de Aulas, Talleres y Laboratorios",
                    "Laboratorios y talleres de cada escuela en las tres sedes; auditorios en la sede Central.",
                    List.of("Disponibilidad por sede, pabellón y escuela profesional.",
                            "Tolerancia horaria de 15 min y normativa de uso compartido."),
                    "Revisar normativa de espacios"),
            new AndraArea("fth-box", "Equipamiento", "Préstamo de Equipos y Custodia Universitaria",
                    "Reglamento para equipos de sonido, instrumentos musicales e implementos deportivos.",
                    List.of("Verificación de entrega física y registro de código de custodia.",
                            "Penalizaciones automáticas por devolución tardía o deterioro."),
                    "Ver reglamento de préstamos"));

    public static final List<AndraGuia> ANDRA_GUIAS = List.of(
            new AndraGuia("Paso A", "Subsanar expediente observado", List.of("Ubica la alerta en la pantalla 'Mis Solicitudes'.",
                    "Pulsa 'Subsanar' y carga el documento en PDF solicitado.", "El expediente se reactivará y el plazo continuará."),
                    "Ver manual de subsanación", "solicitudes"),
            new AndraGuia("Paso B", "Verificar validez ante SUNEDU", List.of("Descarga el certificado emitido en formato digital.",
                    "Escanea el código QR institucional con tu celular.", "Comprueba el certificado digital y firma en el portal oficial."),
                    "Ver instrucciones de verificación", "documentos"),
            new AndraGuia("Paso C", "Desistir trámite y reembolso", List.of("Verifica que el estado aún se encuentre 'En trámite'.",
                    "Selecciona 'Desistir' antes de la aprobación de secretaría.", "Tesorería reintegrará la tasa en 5 días hábiles a tu cuenta."),
                    "Ver políticas de reembolso", "solicitudes"));

    public static final List<AndraPregunta> ANDRA_PREGUNTAS = List.of(
            new AndraPregunta("Expedientes y plazos", "¿Por qué mi solicitud figura como 'Observada' y cuánto tiempo tengo para subsanarla?",
                    "Un trámite es observado cuando la secretaría académica detecta que falta un requisito reglamentario (por ejemplo, visado médico institucional o tasa administrativa complementaria). Cuentas con un plazo máximo improrrogable de 36 horas útiles para adjuntar el sustento en 'Mis Solicitudes' antes de que el trámite caduque automáticamente."),
            new AndraPregunta("Validez legal y SUNEDU", "¿Los documentos emitidos en esta plataforma tienen validez ante SUNEDU y el extranjero?",
                    "Sí. Toda constancia y certificado digital emitido incorpora firma digital con certificado digital acreditado por Indecopi y código de verificación QR amparado por la Ley de Firmas Digitales del Perú. Para trámites en el extranjero (Apostilla de La Haya), puedes solicitar el formato con firma manuscrita legalizada ante Secretaría General."),
            new AndraPregunta("Reservas y custodia", "¿Puedo anular una reserva de aula o préstamo de equipo sin incurrir en penalizaciones?",
                    "Puedes cancelar cualquier reserva hasta con 30 minutos de anticipación desde el panel de 'Mis Solicitudes' o 'Reservas'. Si no te presentas sin avisar tras 15 minutos de iniciada la hora reservada, el sistema registrará una falta administrativa y bloqueará la separación de nuevos recursos por 14 días calendario."),
            new AndraPregunta("Pagos y tasas bancarias", "¿Cuáles son los métodos de pago aceptados para las tasas de trámites académicos?",
                    "Puedes abonar las tasas mediante tarjeta de débito o crédito (Visa, Mastercard, Diners), banca por internet de los principales bancos nacionales, o directamente con cargo a tu boleta de pensión mensual universitaria si te encuentras matriculado en el ciclo regular vigente."),
            new AndraPregunta("Equipos universitarios", "¿Qué debo hacer si extravié un equipo prestado o sufre algún desperfecto durante el uso?",
                    "Debes acercarte de inmediato a la jefatura de portería o pulsar 'Reportar daño / falla' en la sección Equipos. Se levantará un acta de conformidad técnica con el especialista y se evaluará si la contingencia queda cubierta por la póliza universitaria o si corresponde la reposición del implemento."));

    public static final List<String> ANDRA_POPULARES = List.of("Subsanación", "Constancia con QR", "Préstamo", "Devolución de tasa", "SUNEDU");

    public List<AndraArea> andraAreas(String andraConsulta) {
        return ANDRA_AREAS.stream().filter(andraArea -> AndraTextoPlano.andraContiene(andraArea.andraTexto(), andraConsulta)).toList();
    }

    public List<AndraGuia> andraGuias(String andraConsulta) {
        return ANDRA_GUIAS.stream().filter(andraGuia -> AndraTextoPlano.andraContiene(andraGuia.andraTexto(), andraConsulta)).toList();
    }

    public List<AndraPregunta> andraPreguntas(String andraConsulta) {
        return ANDRA_PREGUNTAS.stream().filter(andraPregunta -> AndraTextoPlano.andraContiene(andraPregunta.andraTexto(), andraConsulta)).toList();
    }

    public static String andraRespuestaChat(String andraMensaje) {
        String andraTexto = andraMensaje.toLowerCase();
        if (andraTexto.matches("(?s).*(subsan|observ).*")) {
            return "Para subsanar, entra a 'Mis Solicitudes' y pulsa 'Subsanar ahora'. Tienes 36 horas útiles.";
        }
        if (andraTexto.matches("(?s).*(tasa|pago|reemb).*")) {
            return "Las tasas se reintegran en 5 días hábiles tras desistir de un trámite en curso.";
        }
        return "Gracias. Un operador revisará tu consulta y te responderá por este medio.";
    }
}
