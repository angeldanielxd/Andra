package pe.andra.util;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public final class DixonPortapapeles {

    private DixonPortapapeles() {
    }

    public static void dixonCopiar(String dixonTexto) {
        ClipboardContent dixonContenido = new ClipboardContent();
        dixonContenido.putString(dixonTexto);
        Clipboard.getSystemClipboard().setContent(dixonContenido);
    }
}
