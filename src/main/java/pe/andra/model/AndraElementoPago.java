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
public class AndraElementoPago {
    @Column(name = "concepto", nullable = false, length = 160)
    private String andraConcepto;

    @Column(name = "cantidad", nullable = false)
    private int andraCantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal andraPrecioUnitario;

    public BigDecimal andraObtenerImporte() {
        return andraPrecioUnitario.multiply(BigDecimal.valueOf(andraCantidad));
    }
}
