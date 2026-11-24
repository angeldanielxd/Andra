package pe.andra.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class AndraHash {

    private AndraHash() {
    }

    public static long andraNumero(String andraTexto) {
        long andraAlto = 0;
        String andraTexto2 = String.valueOf(andraTexto);
        for (int andraIndice = 0; andraIndice < andraTexto2.length(); ) {
            int andraPuntoCodigo = andraTexto2.codePointAt(andraIndice);
            char andraPrimero = andraTexto2.charAt(andraIndice);
            andraAlto = (andraAlto * 31 + andraPrimero) & 0xFFFFFFFFL;
            andraIndice += Character.charCount(andraPuntoCodigo);
        }
        return andraAlto;
    }

    public static byte[] andraSha256(String andraTexto) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(andraTexto.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException andraError) {
            throw new IllegalStateException(andraError);
        }
    }

    public static String andraSha256Hex(String andraTexto) {
        return HexFormat.of().formatHex(andraSha256(andraTexto));
    }
}
