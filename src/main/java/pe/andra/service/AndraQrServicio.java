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

public final class AndraQrServicio {

    private AndraQrServicio() {
    }

    public static BitMatrix andraMatriz(String andraContenido, int andraTamanio) {
        try {
            return new QRCodeWriter().encode(andraContenido, BarcodeFormat.QR_CODE, andraTamanio, andraTamanio,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 1));
        } catch (WriterException andraError) {
            throw new IllegalStateException(andraError);
        }
    }

    public static Image andraImagen(String andraContenido, int andraTamanio) {
        BitMatrix andraMatriz = andraMatriz(andraContenido, andraTamanio);
        WritableImage andraImagen = new WritableImage(andraMatriz.getWidth(), andraMatriz.getHeight());
        PixelWriter andraEscritorPixeles = andraImagen.getPixelWriter();
        Color andraOscuro = Color.web("#1D1D1F");
        for (int andraPosicionY = 0; andraPosicionY < andraMatriz.getHeight(); andraPosicionY++) {
            for (int andraPosicionX = 0; andraPosicionX < andraMatriz.getWidth(); andraPosicionX++) {
                andraEscritorPixeles.setColor(andraPosicionX, andraPosicionY, andraMatriz.get(andraPosicionX, andraPosicionY) ? andraOscuro : Color.WHITE);
            }
        }
        return andraImagen;
    }

    public static BufferedImage andraImagenAwt(String andraContenido, int andraTamanio) {
        return MatrixToImageWriter.toBufferedImage(andraMatriz(andraContenido, andraTamanio));
    }
}
