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
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonHash;
import pe.andra.util.DixonMontos;

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
public class DixonDocumentoGeneradoServicio {

    public record DixonSemestre(String dixonPeriodo, int dixonCreditos, String dixonPromedio) {
    }

    public record DixonDocumentoOficial(String dixonPlantilla, String dixonTitulo, DixonDatosSolicitante dixonSolicitante, String dixonEscuela,
                                   String dixonRol, String dixonSede, String dixonUso, String dixonFecha, String dixonVerificacion,
                                   List<DixonSemestre> dixonSemestres, String dixonContenidoQr, byte[] dixonFoto) {
        public boolean dixonConCodigo() {
            return dixonSolicitante.getDixonCodigo() != null && !dixonSolicitante.getDixonCodigo().isBlank()
                    && List.of("Estudiante", "Egresado").contains(dixonSolicitante.getDixonTipo());
        }
    }

    private static final Color DIXON_AZUL = new Color(0x00, 0x71, 0xE3);
    private static final Color DIXON_TEXTO = new Color(0x1D, 0x1D, 0x1F);
    private static final Color DIXON_GRIS = new Color(0x6E, 0x6E, 0x73);
    private static final Color DIXON_BORDE = new Color(0xD2, 0xD2, 0xD7);

    private final DixonCatalogoServicio dixonCatalogo;

    public static String dixonVerificacion(String dixonCodigoPago) {
        String dixonTexto = Long.toString(DixonHash.dixonNumero(dixonCodigoPago), 36).toUpperCase();
        return "AND-" + dixonTexto.substring(0, Math.min(6, dixonTexto.length()));
    }

    public DixonDatosSolicitante dixonSolicitante(DixonPago dixonPago) {
        if (dixonPago.getDixonSolicitante() != null && dixonPago.getDixonSolicitante().getDixonNombre() != null) {
            return dixonPago.getDixonSolicitante();
        }
        DixonPersona dixonPersona = dixonPago.getDixonPersona();
        return DixonDatosSolicitante.builder().dixonTipo(dixonPersona.getDixonTipo()).dixonNombre(dixonPersona.getDixonNombre()).dixonDni(dixonPersona.getDixonDni() == null ? "—" : dixonPersona.getDixonDni())
                .dixonCodigo(dixonPersona.getDixonCodigo()).dixonEscuela(dixonPersona.getDixonEscuela() == null ? null : dixonPersona.getDixonEscuela().getDixonCodigo())
                .dixonCorreo(dixonPersona.getDixonCorreo()).build();
    }

    public DixonDocumentoOficial dixonConstruir(DixonPago dixonPago) {
        DixonTramite dixonTramite = dixonPago.getDixonTramite();
        DixonDatosSolicitante dixonDatosSolicitante = dixonSolicitante(dixonPago);
        String dixonEscuela = dixonCatalogo.dixonNombreEscuela(dixonDatosSolicitante.getDixonEscuela());
        String dixonRol = switch (dixonDatosSolicitante.getDixonTipo() == null ? "" : dixonDatosSolicitante.getDixonTipo()) {
            case "Estudiante" -> "es estudiante de la Escuela Profesional de " + dixonEscuela;
            case "Egresado" -> "es egresado(a) de la Escuela Profesional de " + dixonEscuela;
            case "Docente" -> "es docente de la Escuela Profesional de " + dixonEscuela;
            case "Administrativo" -> "labora como personal administrativo"
                    + (dixonDatosSolicitante.getDixonEscuela() != null && !dixonDatosSolicitante.getDixonEscuela().isBlank() ? " en " + dixonDatosSolicitante.getDixonEscuela() : "");
            default -> "ha solicitado el presente documento como público en general";
        };
        List<DixonSemestre> dixonSemestres = new ArrayList<>();
        if (dixonTramite != null && "record".equals(dixonTramite.getDixonPlantilla())) {
            String[] dixonPeriodos = {"2024-2", "2025-1", "2025-2", "2026-1"};
            for (int dixonIndice = 0; dixonIndice < dixonPeriodos.length; dixonIndice++) {
                double dixonProm = 14.2 + (DixonHash.dixonNumero(dixonDatosSolicitante.getDixonDni() + dixonPeriodos[dixonIndice]) % 25) / 10.0;
                dixonSemestres.add(new DixonSemestre(dixonPeriodos[dixonIndice], 20 + dixonIndice % 2, String.format(java.util.Locale.US, "%.2f", dixonProm)));
            }
        }
        String dixonFecha = DixonFechas.dixonLargoSinDia((dixonPago.getDixonPagado() != null ? dixonPago.getDixonPagado() : DixonFechas.dixonAhora()).toLocalDate());
        String dixonUso = (dixonPago.getDixonMotivo() == null || dixonPago.getDixonMotivo().isBlank() ? "trámite personal" : dixonPago.getDixonMotivo()).toLowerCase();
        return new DixonDocumentoOficial(dixonTramite == null ? "constancia" : dixonTramite.getDixonPlantilla(), dixonTramite == null ? dixonPago.getDixonConcepto().toUpperCase()
                : dixonTramite.getDixonNombre().toUpperCase(), dixonDatosSolicitante, dixonEscuela, dixonRol, dixonPago.getDixonSede().getDixonNombre(), dixonUso, dixonFecha,
                dixonVerificacion(dixonPago.getDixonCodigo()), dixonSemestres, "ANDRA-DOC|" + dixonPago.getDixonCodigo() + "|DEMO", dixonPago.getDixonFoto());
    }

    public byte[] dixonPdfDocumento(DixonPago dixonPago) {
        DixonDocumentoOficial dixonDocumentoOficial = dixonConstruir(dixonPago);
        return dixonCrear(dixonLienzo -> {
            if ("carne".equals(dixonDocumentoOficial.dixonPlantilla())) {
                dixonCarnePdf(dixonLienzo, dixonDocumentoOficial);
                return;
            }
            dixonLienzo.dixonEncabezadoInstitucion();
            dixonLienzo.dixonTitulo(dixonDocumentoOficial.dixonTitulo());
            StringBuilder dixonParrafo = new StringBuilder("La Dirección de Servicios Académicos hace constar que ")
                    .append(dixonDocumentoOficial.dixonSolicitante().getDixonNombre()).append(", identificado(a) con DNI N.° ").append(dixonDocumentoOficial.dixonSolicitante().getDixonDni());
            if (dixonDocumentoOficial.dixonConCodigo()) {
                dixonParrafo.append(" y código universitario N.° ").append(dixonDocumentoOficial.dixonSolicitante().getDixonCodigo());
            }
            dixonParrafo.append(", ").append(dixonDocumentoOficial.dixonRol()).append(", sede ").append(dixonDocumentoOficial.dixonSede()).append(".");
            dixonLienzo.dixonParrafo(dixonParrafo.toString(), 11.5f);
            if (!dixonDocumentoOficial.dixonSemestres().isEmpty()) {
                dixonLienzo.dixonTabla(new String[]{"Semestre", "Créditos", "Promedio"}, dixonDocumentoOficial.dixonSemestres().stream()
                        .map(dixonPosicionX -> new String[]{dixonPosicionX.dixonPeriodo(), String.valueOf(dixonPosicionX.dixonCreditos()), dixonPosicionX.dixonPromedio()}).toList(), new float[]{0.5f, 0.25f, 0.25f});
            }
            if ("certificado".equals(dixonDocumentoOficial.dixonPlantilla())) {
                dixonLienzo.dixonParrafo("Se certifica que ha aprobado las asignaturas correspondientes a los semestres 2024-2 al 2026-1, conforme al detalle registrado en el sistema académico.", 11.5f);
            }
            dixonLienzo.dixonParrafo("Se expide el presente documento a solicitud del interesado(a) para fines de " + dixonDocumentoOficial.dixonUso() + ".", 11.5f);
            dixonLienzo.dixonDerecha(dixonDocumentoOficial.dixonFecha());
            dixonLienzo.dixonFirmaYQr(dixonDocumentoOficial.dixonContenidoQr(), "Verificación: " + dixonDocumentoOficial.dixonVerificacion());
        });
    }

    private void dixonCarnePdf(DixonLienzo dixonLienzo, DixonDocumentoOficial dixonDocumentoOficial) throws IOException {
        float dixonPosicionX = 120, dixonPosicionY = 520, dixonAncho = 355, dixonAlto = 224;
        dixonLienzo.dixonRectangulo(dixonPosicionX, dixonPosicionY, dixonAncho, dixonAlto, Color.WHITE, DIXON_BORDE);
        dixonLienzo.dixonRectangulo(dixonPosicionX, dixonPosicionY + dixonAlto - 52, dixonAncho, 52, DIXON_AZUL, null);
        dixonLienzo.dixonTexto("Institución", dixonPosicionX + 18, dixonPosicionY + dixonAlto - 24, true, 14, Color.WHITE);
        dixonLienzo.dixonTexto("Carné universitario 2026-2", dixonPosicionX + 18, dixonPosicionY + dixonAlto - 40, false, 10, Color.WHITE);
        dixonLienzo.dixonRectangulo(dixonPosicionX + 18, dixonPosicionY + 40, 82, 102, new Color(0xE8, 0xE8, 0xED), null);
        if (dixonDocumentoOficial.dixonFoto() != null && dixonDocumentoOficial.dixonFoto().length > 0) {
            BufferedImage dixonImagen = ImageIO.read(new ByteArrayInputStream(dixonDocumentoOficial.dixonFoto()));
            if (dixonImagen != null) {
                dixonLienzo.dixonImagen(dixonImagen, dixonPosicionX + 18, dixonPosicionY + 40, 82, 102);
            }
        }
        float dixonPosicionYTexto = dixonPosicionY + 128;
        dixonLienzo.dixonTexto(dixonDocumentoOficial.dixonSolicitante().getDixonNombre(), dixonPosicionX + 116, dixonPosicionYTexto, true, 12, DIXON_TEXTO);
        dixonLienzo.dixonTexto("DNI " + dixonDocumentoOficial.dixonSolicitante().getDixonDni(), dixonPosicionX + 116, dixonPosicionYTexto - 18, false, 10, DIXON_TEXTO);
        dixonLienzo.dixonTexto("Código " + (dixonDocumentoOficial.dixonSolicitante().getDixonCodigo() == null ? "—" : dixonDocumentoOficial.dixonSolicitante().getDixonCodigo()), dixonPosicionX + 116, dixonPosicionYTexto - 33, false, 10, DIXON_TEXTO);
        dixonLienzo.dixonTexto("EP " + dixonDocumentoOficial.dixonEscuela(), dixonPosicionX + 116, dixonPosicionYTexto - 48, false, 10, DIXON_TEXTO);
        dixonLienzo.dixonTexto("Sede " + dixonDocumentoOficial.dixonSede(), dixonPosicionX + 116, dixonPosicionYTexto - 63, false, 10, DIXON_TEXTO);
        dixonLienzo.dixonTexto("Válido hasta 31/03/2027", dixonPosicionX + 18, dixonPosicionY + 14, false, 9, DIXON_GRIS);
        dixonLienzo.dixonTexto(dixonDocumentoOficial.dixonVerificacion(), dixonPosicionX + dixonAncho - 90, dixonPosicionY + 14, false, 9, DIXON_GRIS);
    }

    public byte[] dixonPdfComprobante(DixonPago dixonPago) {
        DixonDatosSolicitante dixonDatosSolicitante = dixonSolicitante(dixonPago);
        boolean dixonPagado = dixonPago.dixonEsPagado() || dixonPago.getDixonEstado() == DixonEstadoPago.REEMBOLSADO;
        return dixonCrear(dixonLienzo -> {
            dixonLienzo.dixonTexto("Andra Pay", 56, dixonLienzo.dixonY, true, 16, DIXON_TEXTO);
            dixonLienzo.dixonTexto("tus trámites más simples", 56, dixonLienzo.dixonY - 15, false, 10, DIXON_GRIS);
            dixonLienzo.dixonTextoDerecha(dixonPagado ? "COMPROBANTE DE PAGO" : "ORDEN DE PAGO", 540, dixonLienzo.dixonY, false, 9, DIXON_GRIS);
            dixonLienzo.dixonTextoDerecha(dixonPagado ? dixonPago.getDixonBoleta() : dixonPago.getDixonCodigo(), 540, dixonLienzo.dixonY - 16, true, 14, DIXON_TEXTO);
            if (dixonPagado) {
                dixonLienzo.dixonTextoDerecha("Op. " + dixonPago.getDixonOperacion(), 540, dixonLienzo.dixonY - 30, false, 9, DIXON_GRIS);
            }
            dixonLienzo.dixonY -= 44;
            dixonLienzo.dixonLinea();
            dixonLienzo.dixonY -= 18;
            dixonLienzo.dixonTexto("SOLICITANTE", 56, dixonLienzo.dixonY, true, 8, DIXON_GRIS);
            dixonLienzo.dixonTexto(dixonPagado ? "FECHA DE PAGO" : "EMITIDA", 380, dixonLienzo.dixonY, true, 8, DIXON_GRIS);
            dixonLienzo.dixonY -= 15;
            dixonLienzo.dixonTexto(dixonDatosSolicitante.getDixonNombre(), 56, dixonLienzo.dixonY, true, 11, DIXON_TEXTO);
            dixonLienzo.dixonTexto(dixonPagado ? DixonFechas.dixonDmyHm(dixonPago.getDixonPagado()) : DixonFechas.dixonDmy(dixonPago.getDixonCreado().toLocalDate()), 380, dixonLienzo.dixonY, true, 11, DIXON_TEXTO);
            dixonLienzo.dixonY -= 14;
            dixonLienzo.dixonTexto("DNI " + dixonDatosSolicitante.getDixonDni() + (dixonDatosSolicitante.getDixonCodigo() != null && !dixonDatosSolicitante.getDixonCodigo().isBlank() ? " · Código " + dixonDatosSolicitante.getDixonCodigo() : ""), 56, dixonLienzo.dixonY, false, 10, DIXON_GRIS);
            dixonLienzo.dixonTexto(dixonPagado ? "MÉTODO" : "VENCE", 380, dixonLienzo.dixonY - 4, true, 8, DIXON_GRIS);
            dixonLienzo.dixonY -= 14;
            String dixonTipo = dixonDatosSolicitante.getDixonTipo() + (dixonCatalogo.dixonEscuela(dixonDatosSolicitante.getDixonEscuela()).isPresent() ? " · EP " + dixonCatalogo.dixonNombreEscuela(dixonDatosSolicitante.getDixonEscuela()) : "");
            dixonLienzo.dixonTexto(dixonTipo, 56, dixonLienzo.dixonY, false, 10, DIXON_GRIS);
            dixonLienzo.dixonTexto(dixonPagado ? dixonPago.getDixonMetodo().dixonObtenerEtiqueta() : DixonFechas.dixonDmy(dixonPago.getDixonVence()), 380, dixonLienzo.dixonY - 4, true, 11, DIXON_TEXTO);
            dixonLienzo.dixonY -= 26;
            List<String[]> dixonFilas = dixonPago.getDixonElementos().stream().map(dixonElementoPago -> new String[]{dixonElementoPago.getDixonConcepto(), String.valueOf(dixonElementoPago.getDixonCantidad()),
                    DixonMontos.dixonSoles(dixonElementoPago.getDixonPrecioUnitario()), DixonMontos.dixonSoles(dixonElementoPago.dixonObtenerImporte())}).toList();
            dixonLienzo.dixonTabla(new String[]{"Concepto", "Cant.", "P. unit.", "Importe"}, dixonFilas, new float[]{0.52f, 0.12f, 0.18f, 0.18f});
            dixonLienzo.dixonTexto("Total a pagar", 56, dixonLienzo.dixonY, true, 12, DIXON_TEXTO);
            dixonLienzo.dixonTextoDerecha(DixonMontos.dixonSoles(dixonPago.dixonObtenerTotal()), 540, dixonLienzo.dixonY, true, 12, DIXON_TEXTO);
            dixonLienzo.dixonY -= 24;
            dixonLienzo.dixonParrafo((dixonPagado ? "Pago confirmado. " : "") + "Sede de atención: " + dixonPago.getDixonSede().getDixonNombreCompleto()
                    + ". Montos referenciales de demostración; no se realizan cargos reales.", 9.5f);
        });
    }

    public byte[] dixonPdfSimple(String dixonTitulo, String dixonMeta) {
        return dixonCrear(dixonLienzo -> {
            dixonLienzo.dixonTexto("Andra", 56, dixonLienzo.dixonY, true, 12, DIXON_TEXTO);
            dixonLienzo.dixonTextoDerecha("PDF", 540, dixonLienzo.dixonY, true, 10, new Color(0xD7, 0x00, 0x15));
            dixonLienzo.dixonY -= 14;
            dixonLienzo.dixonLinea();
            dixonLienzo.dixonY -= 24;
            dixonLienzo.dixonParrafoNegrita(dixonTitulo, 16f);
            if (dixonMeta != null && !dixonMeta.isBlank()) {
                dixonLienzo.dixonParrafo(dixonMeta, 10.5f);
            }
            dixonLienzo.dixonParrafo("Documento de demostración generado por Andra. En el sistema real aquí estaría el contenido oficial del archivo.", 10.5f);
        });
    }

    private interface DixonDibujo {
        void dixonDibujar(DixonLienzo dixonLienzo) throws IOException;
    }

    private byte[] dixonCrear(DixonDibujo dixonDibujo) {
        try (PDDocument dixonDoc = new PDDocument(); ByteArrayOutputStream dixonSalida = new ByteArrayOutputStream()) {
            PDPage dixonPagina = new PDPage(PDRectangle.A4);
            dixonDoc.addPage(dixonPagina);
            try (PDPageContentStream dixonContenido = new PDPageContentStream(dixonDoc, dixonPagina)) {
                DixonLienzo dixonLienzo = new DixonLienzo(dixonDoc, dixonContenido);
                dixonDibujo.dixonDibujar(dixonLienzo);
                dixonLienzo.dixonMarcaDeAgua();
            }
            dixonDoc.save(dixonSalida);
            return dixonSalida.toByteArray();
        } catch (IOException dixonError) {
            throw new UncheckedIOException("No se pudo generar el PDF", dixonError);
        }
    }

    private static final class DixonLienzo {
        private final PDDocument dixonDoc;
        private final PDPageContentStream dixonCs;
        private final PDFont dixonNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDFont dixonNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private float dixonY = 780;

        DixonLienzo(PDDocument dixonDoc, PDPageContentStream dixonContenido) {
            this.dixonDoc = dixonDoc;
            this.dixonCs = dixonContenido;
        }

        void dixonTexto(String dixonTexto, float dixonPosicionX, float dixonPosicionY, boolean dixonNegrita2, float dixonTam, Color dixonColor) throws IOException {
            dixonCs.beginText();
            dixonCs.setFont(dixonNegrita2 ? dixonNegrita : dixonNormal, dixonTam);
            dixonCs.setNonStrokingColor(dixonColor);
            dixonCs.newLineAtOffset(dixonPosicionX, dixonPosicionY);
            dixonCs.showText(dixonLimpiar(dixonTexto));
            dixonCs.endText();
        }

        void dixonTextoDerecha(String dixonTexto, float dixonDerecha, float dixonPosicionY, boolean dixonNegrita2, float dixonTam, Color dixonColor) throws IOException {
            float dixonAncho = (dixonNegrita2 ? dixonNegrita : dixonNormal).getStringWidth(dixonLimpiar(dixonTexto)) / 1000 * dixonTam;
            dixonTexto(dixonTexto, dixonDerecha - dixonAncho, dixonPosicionY, dixonNegrita2, dixonTam, dixonColor);
        }

        void dixonLinea() throws IOException {
            dixonCs.setStrokingColor(DIXON_BORDE);
            dixonCs.setLineWidth(1);
            dixonCs.moveTo(56, dixonY);
            dixonCs.lineTo(540, dixonY);
            dixonCs.stroke();
        }

        void dixonEncabezadoInstitucion() throws IOException {
            dixonTexto("Institución", 56, dixonY, true, 13, DIXON_TEXTO);
            dixonTexto("Dirección de Servicios Académicos · Andra", 56, dixonY - 15, false, 10, DIXON_GRIS);
            dixonY -= 26;
            dixonCs.setStrokingColor(DIXON_AZUL);
            dixonCs.setLineWidth(2);
            dixonCs.moveTo(56, dixonY);
            dixonCs.lineTo(540, dixonY);
            dixonCs.stroke();
            dixonY -= 40;
        }

        void dixonTitulo(String dixonTexto) throws IOException {
            float dixonTam = 15;
            float dixonAncho = dixonNegrita.getStringWidth(dixonLimpiar(dixonTexto)) / 1000 * dixonTam;
            dixonTexto(dixonTexto, (595 - dixonAncho) / 2, dixonY, true, dixonTam, DIXON_TEXTO);
            dixonY -= 34;
        }

        void dixonDerecha(String dixonTexto) throws IOException {
            dixonTextoDerecha(dixonTexto, 540, dixonY, false, 11.5f, DIXON_TEXTO);
            dixonY -= 40;
        }

        void dixonParrafo(String dixonTexto, float dixonTam) throws IOException {
            dixonEscribir(dixonTexto, dixonTam, dixonNormal);
        }

        void dixonParrafoNegrita(String dixonTexto, float dixonTam) throws IOException {
            dixonEscribir(dixonTexto, dixonTam, dixonNegrita);
        }

        private void dixonEscribir(String dixonTexto, float dixonTam, PDFont dixonFuente) throws IOException {
            for (String dixonLinea : dixonPartir(dixonLimpiar(dixonTexto), dixonFuente, dixonTam, 484)) {
                dixonTexto(dixonLinea, 56, dixonY, dixonFuente == dixonNegrita, dixonTam, DIXON_TEXTO);
                dixonY -= dixonTam * 1.6f;
            }
            dixonY -= 8;
        }

        private List<String> dixonPartir(String dixonTexto, PDFont dixonFuente, float dixonTam, float dixonAncho) throws IOException {
            List<String> dixonLineas = new ArrayList<>();
            StringBuilder dixonActual = new StringBuilder();
            for (String dixonPalabra : dixonTexto.split(" ")) {
                String dixonPrueba = dixonActual.isEmpty() ? dixonPalabra : dixonActual + " " + dixonPalabra;
                if (dixonFuente.getStringWidth(dixonPrueba) / 1000 * dixonTam > dixonAncho && !dixonActual.isEmpty()) {
                    dixonLineas.add(dixonActual.toString());
                    dixonActual = new StringBuilder(dixonPalabra);
                } else {
                    dixonActual = new StringBuilder(dixonPrueba);
                }
            }
            if (!dixonActual.isEmpty()) {
                dixonLineas.add(dixonActual.toString());
            }
            return dixonLineas;
        }

        void dixonTabla(String[] dixonCabecera, List<String[]> dixonFilas, float[] dixonProporciones) throws IOException {
            float dixonAncho = 484;
            float dixonPosicionX = 56;
            for (int dixonIndice = 0; dixonIndice < dixonCabecera.length; dixonIndice++) {
                if (dixonIndice == 0) {
                    dixonTexto(dixonCabecera[dixonIndice].toUpperCase(), dixonPosicionX, dixonY, true, 8, DIXON_GRIS);
                } else {
                    dixonTextoDerecha(dixonCabecera[dixonIndice].toUpperCase(), dixonPosicionX + dixonAncho * dixonProporciones[dixonIndice], dixonY, true, 8, DIXON_GRIS);
                }
                dixonPosicionX += dixonAncho * dixonProporciones[dixonIndice];
            }
            dixonY -= 8;
            dixonLinea();
            dixonY -= 16;
            for (String[] dixonFila : dixonFilas) {
                dixonPosicionX = 56;
                for (int dixonIndice = 0; dixonIndice < dixonFila.length; dixonIndice++) {
                    if (dixonIndice == 0) {
                        dixonTexto(dixonFila[dixonIndice], dixonPosicionX, dixonY, false, 10, DIXON_TEXTO);
                    } else {
                        dixonTextoDerecha(dixonFila[dixonIndice], dixonPosicionX + dixonAncho * dixonProporciones[dixonIndice], dixonY, false, 10, DIXON_TEXTO);
                    }
                    dixonPosicionX += dixonAncho * dixonProporciones[dixonIndice];
                }
                dixonY -= 8;
                dixonLinea();
                dixonY -= 16;
            }
            dixonY -= 6;
        }

        void dixonFirmaYQr(String dixonContenidoQr, String dixonLeyenda) throws IOException {
            float dixonBase = Math.min(dixonY, 300);
            dixonCs.setStrokingColor(DIXON_TEXTO);
            dixonCs.setLineWidth(0.8f);
            dixonCs.moveTo(90, dixonBase);
            dixonCs.lineTo(270, dixonBase);
            dixonCs.stroke();
            dixonTexto("Firma digital", 145, dixonBase - 14, false, 10, DIXON_TEXTO);
            dixonTexto("Director(a) de Servicios Académicos", 100, dixonBase - 28, false, 8.5f, DIXON_GRIS);
            dixonImagen(DixonQrServicio.dixonImagenAwt(dixonContenidoQr, 240), 440, dixonBase - 30, 96, 96);
            dixonTexto(dixonLeyenda, 430, dixonBase - 44, false, 8.5f, DIXON_GRIS);
            dixonY = dixonBase - 60;
        }

        void dixonRectangulo(float dixonPosicionX, float dixonPosicionY, float dixonAncho, float dixonAlto, Color dixonRelleno, Color dixonBorde) throws IOException {
            dixonCs.addRect(dixonPosicionX, dixonPosicionY, dixonAncho, dixonAlto);
            if (dixonRelleno != null) {
                dixonCs.setNonStrokingColor(dixonRelleno);
                if (dixonBorde != null) {
                    dixonCs.setStrokingColor(dixonBorde);
                    dixonCs.fillAndStroke();
                } else {
                    dixonCs.fill();
                }
            } else if (dixonBorde != null) {
                dixonCs.setStrokingColor(dixonBorde);
                dixonCs.stroke();
            }
        }

        void dixonImagen(BufferedImage dixonImagen, float dixonPosicionX, float dixonPosicionY, float dixonAncho, float dixonAlto) throws IOException {
            PDImageXObject dixonImagenPdf = LosslessFactory.createFromImage(dixonDoc, dixonImagen);
            dixonCs.drawImage(dixonImagenPdf, dixonPosicionX, dixonPosicionY, dixonAncho, dixonAlto);
        }

        void dixonMarcaDeAgua() throws IOException {
            PDExtendedGraphicsState dixonEstadoGrafico = new PDExtendedGraphicsState();
            dixonEstadoGrafico.setNonStrokingAlphaConstant(0.09f);
            dixonCs.saveGraphicsState();
            dixonCs.setGraphicsStateParameters(dixonEstadoGrafico);
            dixonCs.beginText();
            dixonCs.setFont(dixonNegrita, 64);
            dixonCs.setNonStrokingColor(DIXON_AZUL);
            dixonCs.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(-18), 150, 420));
            dixonCs.showText("DEMOSTRACIÓN");
            dixonCs.endText();
            dixonCs.restoreGraphicsState();
        }

        private static String dixonLimpiar(String dixonTexto) {
            if (dixonTexto == null) {
                return "";
            }
            StringBuilder dixonTexto2 = new StringBuilder();
            for (char dixonCaracter : dixonTexto.toCharArray()) {
                dixonTexto2.append(dixonCaracter == '\n' || dixonCaracter == '\t' ? ' ' : dixonCaracter == '→' ? '>' : dixonCaracter);
            }
            return dixonTexto2.toString();
        }
    }
}
