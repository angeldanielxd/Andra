package pe.andra.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class DixonMontos {

    private static final DecimalFormatSymbols DIXON_SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.US);

    private DixonMontos() {
    }

    public static String dixonSoles(BigDecimal dixonMonto) {
        DecimalFormat dixonFormato = new DecimalFormat("#,##0.00", DIXON_SIMBOLOS);
        return "S/ " + dixonFormato.format(dixonMonto == null ? BigDecimal.ZERO : dixonMonto.setScale(2, RoundingMode.HALF_UP));
    }

    public static String dixonSoles(double dixonMonto) {
        return dixonSoles(BigDecimal.valueOf(dixonMonto));
    }

    public static String dixonMiles(long dixonValor) {
        return new DecimalFormat("#,##0", DIXON_SIMBOLOS).format(dixonValor);
    }

    public static String dixonEntero(BigDecimal dixonMonto) {
        return String.valueOf(dixonMonto.setScale(0, RoundingMode.HALF_UP).intValue());
    }
}
