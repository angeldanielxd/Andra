package pe.andra.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.Map;

public final class DixonQrServicio {

    private DixonQrServicio() {
    }

    public static BitMatrix dixonMatriz(String dixonContenido, int dixonTamanio) {
        try {
            return new QRCodeWriter().encode(dixonContenido, BarcodeFormat.QR_CODE, dixonTamanio, dixonTamanio,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 1));
        } catch (WriterException dixonError) {
            throw new IllegalStateException(dixonError);
        }
    }

    public static Image dixonImagen(String dixonContenido, int dixonTamanio) {
        BitMatrix dixonMatriz = dixonMatriz(dixonContenido, dixonTamanio);
        WritableImage dixonImagen = new WritableImage(dixonMatriz.getWidth(), dixonMatriz.getHeight());
        PixelWriter dixonEscritorPixeles = dixonImagen.getPixelWriter();
        Color dixonOscuro = Color.web("#1D1D1F");
        for (int dixonPosicionY = 0; dixonPosicionY < dixonMatriz.getHeight(); dixonPosicionY++) {
            for (int dixonPosicionX = 0; dixonPosicionX < dixonMatriz.getWidth(); dixonPosicionX++) {
                dixonEscritorPixeles.setColor(dixonPosicionX, dixonPosicionY, dixonMatriz.get(dixonPosicionX, dixonPosicionY) ? dixonOscuro : Color.WHITE);
            }
        }
        return dixonImagen;
    }

    public static BufferedImage dixonImagenAwt(String dixonContenido, int dixonTamanio) {
        return MatrixToImageWriter.toBufferedImage(dixonMatriz(dixonContenido, dixonTamanio));
    }
}
