package pe.andra.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DixonElementoPago {
    @Column(name = "concepto", nullable = false, length = 160)
    private String dixonConcepto;

    @Column(name = "cantidad", nullable = false)
    private int dixonCantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal dixonPrecioUnitario;

    public BigDecimal dixonObtenerImporte() {
        return dixonPrecioUnitario.multiply(BigDecimal.valueOf(dixonCantidad));
    }
}
