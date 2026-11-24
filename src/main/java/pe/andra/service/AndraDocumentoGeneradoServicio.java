package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import pe.andra.model.*;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraHash;
import pe.andra.util.AndraMontos;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AndraDocumentoGeneradoServicio {

    public record AndraSemestre(String andraPeriodo, int andraCreditos, String andraPromedio) {
    }

    public record AndraDocumentoOficial(String andraPlantilla, String andraTitulo, AndraDatosSolicitante andraSolicitante, String andraEscuela,
                                   String andraRol, String andraSede, String andraUso, String andraFecha, String andraVerificacion,
                                   List<AndraSemestre> andraSemestres, String andraContenidoQr, byte[] andraFoto) {
        public boolean andraConCodigo() {
            return andraSolicitante.getAndraCodigo() != null && !andraSolicitante.getAndraCodigo().isBlank()
                    && List.of("Estudiante", "Egresado").contains(andraSolicitante.getAndraTipo());
        }
    }

    private static final Color ANDRA_AZUL = new Color(0x00, 0x71, 0xE3);
    private static final Color ANDRA_TEXTO = new Color(0x1D, 0x1D, 0x1F);
    private static final Color ANDRA_GRIS = new Color(0x6E, 0x6E, 0x73);
    private static final Color ANDRA_BORDE = new Color(0xD2, 0xD2, 0xD7);

    private final AndraCatalogoServicio andraCatalogo;

    public static String andraVerificacion(String andraCodigoPago) {
        String andraTexto = Long.toString(AndraHash.andraNumero(andraCodigoPago), 36).toUpperCase();
        return "AND-" + andraTexto.substring(0, Math.min(6, andraTexto.length()));
    }

    public AndraDatosSolicitante andraSolicitante(AndraPago andraPago) {
        if (andraPago.getAndraSolicitante() != null && andraPago.getAndraSolicitante().getAndraNombre() != null) {
            return andraPago.getAndraSolicitante();
        }
        AndraPersona andraPersona = andraPago.getAndraPersona();
        return AndraDatosSolicitante.builder().andraTipo(andraPersona.getAndraTipo()).andraNombre(andraPersona.getAndraNombre()).andraDni(andraPersona.getAndraDni() == null ? "—" : andraPersona.getAndraDni())
                .andraCodigo(andraPersona.getAndraCodigo()).andraEscuela(andraPersona.getAndraEscuela() == null ? null : andraPersona.getAndraEscuela().getAndraCodigo())
                .andraCorreo(andraPersona.getAndraCorreo()).build();
    }

    public AndraDocumentoOficial andraConstruir(AndraPago andraPago) {
        AndraTramite andraTramite = andraPago.getAndraTramite();
        AndraDatosSolicitante andraDatosSolicitante = andraSolicitante(andraPago);
        String andraEscuela = andraCatalogo.andraNombreEscuela(andraDatosSolicitante.getAndraEscuela());
        String andraRol = switch (andraDatosSolicitante.getAndraTipo() == null ? "" : andraDatosSolicitante.getAndraTipo()) {
            case "Estudiante" -> "es estudiante de la Escuela Profesional de " + andraEscuela;
            case "Egresado" -> "es egresado(a) de la Escuela Profesional de " + andraEscuela;
            case "Docente" -> "es docente de la Escuela Profesional de " + andraEscuela;
            case "Administrativo" -> "labora como personal administrativo"
                    + (andraDatosSolicitante.getAndraEscuela() != null && !andraDatosSolicitante.getAndraEscuela().isBlank() ? " en " + andraDatosSolicitante.getAndraEscuela() : "");
            default -> "ha solicitado el presente documento como público en general";
        };
        List<AndraSemestre> andraSemestres = new ArrayList<>();
        if (andraTramite != null && "record".equals(andraTramite.getAndraPlantilla())) {
            String[] andraPeriodos = {"2024-2", "2025-1", "2025-2", "2026-1"};
            for (int andraIndice = 0; andraIndice < andraPeriodos.length; andraIndice++) {
                double andraProm = 14.2 + (AndraHash.andraNumero(andraDatosSolicitante.getAndraDni() + andraPeriodos[andraIndice]) % 25) / 10.0;
                andraSemestres.add(new AndraSemestre(andraPeriodos[andraIndice], 20 + andraIndice % 2, String.format(java.util.Locale.US, "%.2f", andraProm)));
            }
        }
        String andraFecha = AndraFechas.andraLargoSinDia((andraPago.getAndraPagado() != null ? andraPago.getAndraPagado() : AndraFechas.andraAhora()).toLocalDate());
        String andraUso = (andraPago.getAndraMotivo() == null || andraPago.getAndraMotivo().isBlank() ? "trámite personal" : andraPago.getAndraMotivo()).toLowerCase();
        return new AndraDocumentoOficial(andraTramite == null ? "constancia" : andraTramite.getAndraPlantilla(), andraTramite == null ? andraPago.getAndraConcepto().toUpperCase()
                : andraTramite.getAndraNombre().toUpperCase(), andraDatosSolicitante, andraEscuela, andraRol, andraPago.getAndraSede().getAndraNombre(), andraUso, andraFecha,
                andraVerificacion(andraPago.getAndraCodigo()), andraSemestres, "ANDRA-DOC|" + andraPago.getAndraCodigo() + "|DEMO", andraPago.getAndraFoto());
    }

    public byte[] andraPdfDocumento(AndraPago andraPago) {
        AndraDocumentoOficial andraDocumentoOficial = andraConstruir(andraPago);
        return andraCrear(andraLienzo -> {
            if ("carne".equals(andraDocumentoOficial.andraPlantilla())) {
                andraCarnePdf(andraLienzo, andraDocumentoOficial);
                return;
            }
            andraLienzo.andraEncabezadoInstitucion();
            andraLienzo.andraTitulo(andraDocumentoOficial.andraTitulo());
            StringBuilder andraParrafo = new StringBuilder("La Dirección de Servicios Académicos hace constar que ")
                    .append(andraDocumentoOficial.andraSolicitante().getAndraNombre()).append(", identificado(a) con DNI N.° ").append(andraDocumentoOficial.andraSolicitante().getAndraDni());
            if (andraDocumentoOficial.andraConCodigo()) {
                andraParrafo.append(" y código universitario N.° ").append(andraDocumentoOficial.andraSolicitante().getAndraCodigo());
            }
            andraParrafo.append(", ").append(andraDocumentoOficial.andraRol()).append(", sede ").append(andraDocumentoOficial.andraSede()).append(".");
            andraLienzo.andraParrafo(andraParrafo.toString(), 11.5f);
            if (!andraDocumentoOficial.andraSemestres().isEmpty()) {
                andraLienzo.andraTabla(new String[]{"Semestre", "Créditos", "Promedio"}, andraDocumentoOficial.andraSemestres().stream()
                        .map(andraPosicionX -> new String[]{andraPosicionX.andraPeriodo(), String.valueOf(andraPosicionX.andraCreditos()), andraPosicionX.andraPromedio()}).toList(), new float[]{0.5f, 0.25f, 0.25f});
            }
            if ("certificado".equals(andraDocumentoOficial.andraPlantilla())) {
                andraLienzo.andraParrafo("Se certifica que ha aprobado las asignaturas correspondientes a los semestres 2024-2 al 2026-1, conforme al detalle registrado en el sistema académico.", 11.5f);
            }
            andraLienzo.andraParrafo("Se expide el presente documento a solicitud del interesado(a) para fines de " + andraDocumentoOficial.andraUso() + ".", 11.5f);
            andraLienzo.andraDerecha(andraDocumentoOficial.andraFecha());
            andraLienzo.andraFirmaYQr(andraDocumentoOficial.andraContenidoQr(), "Verificación: " + andraDocumentoOficial.andraVerificacion());
        });
    }

    private void andraCarnePdf(AndraLienzo andraLienzo, AndraDocumentoOficial andraDocumentoOficial) throws IOException {
        float andraPosicionX = 120, andraPosicionY = 520, andraAncho = 355, andraAlto = 224;
        andraLienzo.andraRectangulo(andraPosicionX, andraPosicionY, andraAncho, andraAlto, Color.WHITE, ANDRA_BORDE);
        andraLienzo.andraRectangulo(andraPosicionX, andraPosicionY + andraAlto - 52, andraAncho, 52, ANDRA_AZUL, null);
        andraLienzo.andraTexto("Institución", andraPosicionX + 18, andraPosicionY + andraAlto - 24, true, 14, Color.WHITE);
        andraLienzo.andraTexto("Carné universitario 2026-2", andraPosicionX + 18, andraPosicionY + andraAlto - 40, false, 10, Color.WHITE);
        andraLienzo.andraRectangulo(andraPosicionX + 18, andraPosicionY + 40, 82, 102, new Color(0xE8, 0xE8, 0xED), null);
        if (andraDocumentoOficial.andraFoto() != null && andraDocumentoOficial.andraFoto().length > 0) {
            BufferedImage andraImagen = ImageIO.read(new ByteArrayInputStream(andraDocumentoOficial.andraFoto()));
            if (andraImagen != null) {
                andraLienzo.andraImagen(andraImagen, andraPosicionX + 18, andraPosicionY + 40, 82, 102);
            }
        }
        float andraPosicionYTexto = andraPosicionY + 128;
        andraLienzo.andraTexto(andraDocumentoOficial.andraSolicitante().getAndraNombre(), andraPosicionX + 116, andraPosicionYTexto, true, 12, ANDRA_TEXTO);
        andraLienzo.andraTexto("DNI " + andraDocumentoOficial.andraSolicitante().getAndraDni(), andraPosicionX + 116, andraPosicionYTexto - 18, false, 10, ANDRA_TEXTO);
        andraLienzo.andraTexto("Código " + (andraDocumentoOficial.andraSolicitante().getAndraCodigo() == null ? "—" : andraDocumentoOficial.andraSolicitante().getAndraCodigo()), andraPosicionX + 116, andraPosicionYTexto - 33, false, 10, ANDRA_TEXTO);
        andraLienzo.andraTexto("EP " + andraDocumentoOficial.andraEscuela(), andraPosicionX + 116, andraPosicionYTexto - 48, false, 10, ANDRA_TEXTO);
        andraLienzo.andraTexto("Sede " + andraDocumentoOficial.andraSede(), andraPosicionX + 116, andraPosicionYTexto - 63, false, 10, ANDRA_TEXTO);
        andraLienzo.andraTexto("Válido hasta 31/03/2027", andraPosicionX + 18, andraPosicionY + 14, false, 9, ANDRA_GRIS);
        andraLienzo.andraTexto(andraDocumentoOficial.andraVerificacion(), andraPosicionX + andraAncho - 90, andraPosicionY + 14, false, 9, ANDRA_GRIS);
    }

    public byte[] andraPdfComprobante(AndraPago andraPago) {
        AndraDatosSolicitante andraDatosSolicitante = andraSolicitante(andraPago);
        boolean andraPagado = andraPago.andraEsPagado() || andraPago.getAndraEstado() == AndraEstadoPago.REEMBOLSADO;
        return andraCrear(andraLienzo -> {
            andraLienzo.andraTexto("Andra Pay", 56, andraLienzo.andraY, true, 16, ANDRA_TEXTO);
            andraLienzo.andraTexto("tus trámites más simples", 56, andraLienzo.andraY - 15, false, 10, ANDRA_GRIS);
            andraLienzo.andraTextoDerecha(andraPagado ? "COMPROBANTE DE PAGO" : "ORDEN DE PAGO", 540, andraLienzo.andraY, false, 9, ANDRA_GRIS);
            andraLienzo.andraTextoDerecha(andraPagado ? andraPago.getAndraBoleta() : andraPago.getAndraCodigo(), 540, andraLienzo.andraY - 16, true, 14, ANDRA_TEXTO);
            if (andraPagado) {
                andraLienzo.andraTextoDerecha("Op. " + andraPago.getAndraOperacion(), 540, andraLienzo.andraY - 30, false, 9, ANDRA_GRIS);
            }
            andraLienzo.andraY -= 44;
            andraLienzo.andraLinea();
            andraLienzo.andraY -= 18;
            andraLienzo.andraTexto("SOLICITANTE", 56, andraLienzo.andraY, true, 8, ANDRA_GRIS);
            andraLienzo.andraTexto(andraPagado ? "FECHA DE PAGO" : "EMITIDA", 380, andraLienzo.andraY, true, 8, ANDRA_GRIS);
            andraLienzo.andraY -= 15;
            andraLienzo.andraTexto(andraDatosSolicitante.getAndraNombre(), 56, andraLienzo.andraY, true, 11, ANDRA_TEXTO);
            andraLienzo.andraTexto(andraPagado ? AndraFechas.andraDmyHm(andraPago.getAndraPagado()) : AndraFechas.andraDmy(andraPago.getAndraCreado().toLocalDate()), 380, andraLienzo.andraY, true, 11, ANDRA_TEXTO);
            andraLienzo.andraY -= 14;
            andraLienzo.andraTexto("DNI " + andraDatosSolicitante.getAndraDni() + (andraDatosSolicitante.getAndraCodigo() != null && !andraDatosSolicitante.getAndraCodigo().isBlank() ? " · Código " + andraDatosSolicitante.getAndraCodigo() : ""), 56, andraLienzo.andraY, false, 10, ANDRA_GRIS);
            andraLienzo.andraTexto(andraPagado ? "MÉTODO" : "VENCE", 380, andraLienzo.andraY - 4, true, 8, ANDRA_GRIS);
            andraLienzo.andraY -= 14;
            String andraTipo = andraDatosSolicitante.getAndraTipo() + (andraCatalogo.andraEscuela(andraDatosSolicitante.getAndraEscuela()).isPresent() ? " · EP " + andraCatalogo.andraNombreEscuela(andraDatosSolicitante.getAndraEscuela()) : "");
            andraLienzo.andraTexto(andraTipo, 56, andraLienzo.andraY, false, 10, ANDRA_GRIS);
            andraLienzo.andraTexto(andraPagado ? andraPago.getAndraMetodo().andraObtenerEtiqueta() : AndraFechas.andraDmy(andraPago.getAndraVence()), 380, andraLienzo.andraY - 4, true, 11, ANDRA_TEXTO);
            andraLienzo.andraY -= 26;
            List<String[]> andraFilas = andraPago.getAndraElementos().stream().map(andraElementoPago -> new String[]{andraElementoPago.getAndraConcepto(), String.valueOf(andraElementoPago.getAndraCantidad()),
                    AndraMontos.andraSoles(andraElementoPago.getAndraPrecioUnitario()), AndraMontos.andraSoles(andraElementoPago.andraObtenerImporte())}).toList();
            andraLienzo.andraTabla(new String[]{"Concepto", "Cant.", "P. unit.", "Importe"}, andraFilas, new float[]{0.52f, 0.12f, 0.18f, 0.18f});
            andraLienzo.andraTexto("Total a pagar", 56, andraLienzo.andraY, true, 12, ANDRA_TEXTO);
            andraLienzo.andraTextoDerecha(AndraMontos.andraSoles(andraPago.andraObtenerTotal()), 540, andraLienzo.andraY, true, 12, ANDRA_TEXTO);
            andraLienzo.andraY -= 24;
            andraLienzo.andraParrafo((andraPagado ? "Pago confirmado. " : "") + "Sede de atención: " + andraPago.getAndraSede().getAndraNombreCompleto()
                    + ". Montos referenciales de demostración; no se realizan cargos reales.", 9.5f);
        });
    }

    public byte[] andraPdfSimple(String andraTitulo, String andraMeta) {
        return andraCrear(andraLienzo -> {
            andraLienzo.andraTexto("Andra", 56, andraLienzo.andraY, true, 12, ANDRA_TEXTO);
            andraLienzo.andraTextoDerecha("PDF", 540, andraLienzo.andraY, true, 10, new Color(0xD7, 0x00, 0x15));
            andraLienzo.andraY -= 14;
            andraLienzo.andraLinea();
            andraLienzo.andraY -= 24;
            andraLienzo.andraParrafoNegrita(andraTitulo, 16f);
            if (andraMeta != null && !andraMeta.isBlank()) {
                andraLienzo.andraParrafo(andraMeta, 10.5f);
            }
            andraLienzo.andraParrafo("Documento de demostración generado por Andra. En el sistema real aquí estaría el contenido oficial del archivo.", 10.5f);
        });
    }

    private interface AndraDibujo {
        void andraDibujar(AndraLienzo andraLienzo) throws IOException;
    }

    private byte[] andraCrear(AndraDibujo andraDibujo) {
        try (PDDocument andraDoc = new PDDocument(); ByteArrayOutputStream andraSalida = new ByteArrayOutputStream()) {
            PDPage andraPagina = new PDPage(PDRectangle.A4);
            andraDoc.addPage(andraPagina);
            try (PDPageContentStream andraContenido = new PDPageContentStream(andraDoc, andraPagina)) {
                AndraLienzo andraLienzo = new AndraLienzo(andraDoc, andraContenido);
                andraDibujo.andraDibujar(andraLienzo);
                andraLienzo.andraMarcaDeAgua();
            }
            andraDoc.save(andraSalida);
            return andraSalida.toByteArray();
        } catch (IOException andraError) {
            throw new UncheckedIOException("No se pudo generar el PDF", andraError);
        }
    }

    private static final class AndraLienzo {
        private final PDDocument andraDoc;
        private final PDPageContentStream andraCs;
        private final PDFont andraNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDFont andraNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private float andraY = 780;

        AndraLienzo(PDDocument andraDoc, PDPageContentStream andraContenido) {
            this.andraDoc = andraDoc;
            this.andraCs = andraContenido;
        }

        void andraTexto(String andraTexto, float andraPosicionX, float andraPosicionY, boolean andraNegrita2, float andraTam, Color andraColor) throws IOException {
            andraCs.beginText();
            andraCs.setFont(andraNegrita2 ? andraNegrita : andraNormal, andraTam);
            andraCs.setNonStrokingColor(andraColor);
            andraCs.newLineAtOffset(andraPosicionX, andraPosicionY);
            andraCs.showText(andraLimpiar(andraTexto));
            andraCs.endText();
        }

        void andraTextoDerecha(String andraTexto, float andraDerecha, float andraPosicionY, boolean andraNegrita2, float andraTam, Color andraColor) throws IOException {
            float andraAncho = (andraNegrita2 ? andraNegrita : andraNormal).getStringWidth(andraLimpiar(andraTexto)) / 1000 * andraTam;
            andraTexto(andraTexto, andraDerecha - andraAncho, andraPosicionY, andraNegrita2, andraTam, andraColor);
        }

        void andraLinea() throws IOException {
            andraCs.setStrokingColor(ANDRA_BORDE);
            andraCs.setLineWidth(1);
            andraCs.moveTo(56, andraY);
            andraCs.lineTo(540, andraY);
            andraCs.stroke();
        }

        void andraEncabezadoInstitucion() throws IOException {
            andraTexto("Institución", 56, andraY, true, 13, ANDRA_TEXTO);
            andraTexto("Dirección de Servicios Académicos · Andra", 56, andraY - 15, false, 10, ANDRA_GRIS);
            andraY -= 26;
            andraCs.setStrokingColor(ANDRA_AZUL);
            andraCs.setLineWidth(2);
            andraCs.moveTo(56, andraY);
            andraCs.lineTo(540, andraY);
            andraCs.stroke();
            andraY -= 40;
        }

        void andraTitulo(String andraTexto) throws IOException {
            float andraTam = 15;
            float andraAncho = andraNegrita.getStringWidth(andraLimpiar(andraTexto)) / 1000 * andraTam;
            andraTexto(andraTexto, (595 - andraAncho) / 2, andraY, true, andraTam, ANDRA_TEXTO);
            andraY -= 34;
        }

        void andraDerecha(String andraTexto) throws IOException {
            andraTextoDerecha(andraTexto, 540, andraY, false, 11.5f, ANDRA_TEXTO);
            andraY -= 40;
        }

        void andraParrafo(String andraTexto, float andraTam) throws IOException {
            andraEscribir(andraTexto, andraTam, andraNormal);
        }

        void andraParrafoNegrita(String andraTexto, float andraTam) throws IOException {
            andraEscribir(andraTexto, andraTam, andraNegrita);
        }

        private void andraEscribir(String andraTexto, float andraTam, PDFont andraFuente) throws IOException {
            for (String andraLinea : andraPartir(andraLimpiar(andraTexto), andraFuente, andraTam, 484)) {
                andraTexto(andraLinea, 56, andraY, andraFuente == andraNegrita, andraTam, ANDRA_TEXTO);
                andraY -= andraTam * 1.6f;
            }
            andraY -= 8;
        }

        private List<String> andraPartir(String andraTexto, PDFont andraFuente, float andraTam, float andraAncho) throws IOException {
            List<String> andraLineas = new ArrayList<>();
            StringBuilder andraActual = new StringBuilder();
            for (String andraPalabra : andraTexto.split(" ")) {
                String andraPrueba = andraActual.isEmpty() ? andraPalabra : andraActual + " " + andraPalabra;
                if (andraFuente.getStringWidth(andraPrueba) / 1000 * andraTam > andraAncho && !andraActual.isEmpty()) {
                    andraLineas.add(andraActual.toString());
                    andraActual = new StringBuilder(andraPalabra);
                } else {
                    andraActual = new StringBuilder(andraPrueba);
                }
            }
            if (!andraActual.isEmpty()) {
                andraLineas.add(andraActual.toString());
            }
            return andraLineas;
        }

        void andraTabla(String[] andraCabecera, List<String[]> andraFilas, float[] andraProporciones) throws IOException {
            float andraAncho = 484;
            float andraPosicionX = 56;
            for (int andraIndice = 0; andraIndice < andraCabecera.length; andraIndice++) {
                if (andraIndice == 0) {
                    andraTexto(andraCabecera[andraIndice].toUpperCase(), andraPosicionX, andraY, true, 8, ANDRA_GRIS);
                } else {
                    andraTextoDerecha(andraCabecera[andraIndice].toUpperCase(), andraPosicionX + andraAncho * andraProporciones[andraIndice], andraY, true, 8, ANDRA_GRIS);
                }
                andraPosicionX += andraAncho * andraProporciones[andraIndice];
            }
            andraY -= 8;
            andraLinea();
            andraY -= 16;
            for (String[] andraFila : andraFilas) {
                andraPosicionX = 56;
                for (int andraIndice = 0; andraIndice < andraFila.length; andraIndice++) {
                    if (andraIndice == 0) {
                        andraTexto(andraFila[andraIndice], andraPosicionX, andraY, false, 10, ANDRA_TEXTO);
                    } else {
                        andraTextoDerecha(andraFila[andraIndice], andraPosicionX + andraAncho * andraProporciones[andraIndice], andraY, false, 10, ANDRA_TEXTO);
                    }
                    andraPosicionX += andraAncho * andraProporciones[andraIndice];
                }
                andraY -= 8;
                andraLinea();
                andraY -= 16;
            }
            andraY -= 6;
        }

        void andraFirmaYQr(String andraContenidoQr, String andraLeyenda) throws IOException {
            float andraBase = Math.min(andraY, 300);
            andraCs.setStrokingColor(ANDRA_TEXTO);
            andraCs.setLineWidth(0.8f);
            andraCs.moveTo(90, andraBase);
            andraCs.lineTo(270, andraBase);
            andraCs.stroke();
            andraTexto("Firma digital", 145, andraBase - 14, false, 10, ANDRA_TEXTO);
            andraTexto("Director(a) de Servicios Académicos", 100, andraBase - 28, false, 8.5f, ANDRA_GRIS);
            andraImagen(AndraQrServicio.andraImagenAwt(andraContenidoQr, 240), 440, andraBase - 30, 96, 96);
            andraTexto(andraLeyenda, 430, andraBase - 44, false, 8.5f, ANDRA_GRIS);
            andraY = andraBase - 60;
        }

        void andraRectangulo(float andraPosicionX, float andraPosicionY, float andraAncho, float andraAlto, Color andraRelleno, Color andraBorde) throws IOException {
            andraCs.addRect(andraPosicionX, andraPosicionY, andraAncho, andraAlto);
            if (andraRelleno != null) {
                andraCs.setNonStrokingColor(andraRelleno);
                if (andraBorde != null) {
                    andraCs.setStrokingColor(andraBorde);
                    andraCs.fillAndStroke();
                } else {
                    andraCs.fill();
                }
            } else if (andraBorde != null) {
                andraCs.setStrokingColor(andraBorde);
                andraCs.stroke();
            }
        }

        void andraImagen(BufferedImage andraImagen, float andraPosicionX, float andraPosicionY, float andraAncho, float andraAlto) throws IOException {
            PDImageXObject andraImagenPdf = LosslessFactory.createFromImage(andraDoc, andraImagen);
            andraCs.drawImage(andraImagenPdf, andraPosicionX, andraPosicionY, andraAncho, andraAlto);
        }

        void andraMarcaDeAgua() throws IOException {
            PDExtendedGraphicsState andraEstadoGrafico = new PDExtendedGraphicsState();
            andraEstadoGrafico.setNonStrokingAlphaConstant(0.09f);
            andraCs.saveGraphicsState();
            andraCs.setGraphicsStateParameters(andraEstadoGrafico);
            andraCs.beginText();
            andraCs.setFont(andraNegrita, 64);
            andraCs.setNonStrokingColor(ANDRA_AZUL);
            andraCs.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(-18), 150, 420));
            andraCs.showText("DEMOSTRACIÓN");
            andraCs.endText();
            andraCs.restoreGraphicsState();
        }

        private static String andraLimpiar(String andraTexto) {
            if (andraTexto == null) {
                return "";
            }
            StringBuilder andraTexto2 = new StringBuilder();
            for (char andraCaracter : andraTexto.toCharArray()) {
                andraTexto2.append(andraCaracter == '\n' || andraCaracter == '\t' ? ' ' : andraCaracter == '→' ? '>' : andraCaracter);
            }
            return andraTexto2.toString();
        }
    }
}
