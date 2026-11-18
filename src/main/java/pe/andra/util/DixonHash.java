package pe.andra.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class DixonHash {

    private DixonHash() {
    }

    public static long dixonNumero(String dixonTexto) {
        long dixonAlto = 0;
        String dixonTexto2 = String.valueOf(dixonTexto);
        for (int dixonIndice = 0; dixonIndice < dixonTexto2.length(); ) {
            int dixonPuntoCodigo = dixonTexto2.codePointAt(dixonIndice);
            char dixonPrimero = dixonTexto2.charAt(dixonIndice);
            dixonAlto = (dixonAlto * 31 + dixonPrimero) & 0xFFFFFFFFL;
            dixonIndice += Character.charCount(dixonPuntoCodigo);
        }
        return dixonAlto;
    }

    public static byte[] dixonSha256(String dixonTexto) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(dixonTexto.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException dixonError) {
            throw new IllegalStateException(dixonError);
        }
    }

    public static String dixonSha256Hex(String dixonTexto) {
        return HexFormat.of().formatHex(dixonSha256(dixonTexto));
    }
}
