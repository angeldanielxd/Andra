package pe.andra.util;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public final class AndraPortapapeles {

    private AndraPortapapeles() {
    }

    public static void andraCopiar(String andraTexto) {
        ClipboardContent andraContenido = new ClipboardContent();
        andraContenido.putString(andraTexto);
        Clipboard.getSystemClipboard().setContent(andraContenido);
    }
}
