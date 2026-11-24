package pe.andra.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class AndraMontos {

    private static final DecimalFormatSymbols ANDRA_SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.US);

    private AndraMontos() {
    }

    public static String andraSoles(BigDecimal andraMonto) {
        DecimalFormat andraFormato = new DecimalFormat("#,##0.00", ANDRA_SIMBOLOS);
        return "S/ " + andraFormato.format(andraMonto == null ? BigDecimal.ZERO : andraMonto.setScale(2, RoundingMode.HALF_UP));
    }

    public static String andraSoles(double andraMonto) {
        return andraSoles(BigDecimal.valueOf(andraMonto));
    }

    public static String andraMiles(long andraValor) {
        return new DecimalFormat("#,##0", ANDRA_SIMBOLOS).format(andraValor);
    }

    public static String andraEntero(BigDecimal andraMonto) {
        return String.valueOf(andraMonto.setScale(0, RoundingMode.HALF_UP).intValue());
    }
}
