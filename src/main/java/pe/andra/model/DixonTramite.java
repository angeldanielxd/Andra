package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "tramite", uniqueConstraints = @UniqueConstraint(name = "uk_tramite_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonTramite {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String dixonNombre;

    @Column(name = "categoria", nullable = false, length = 40)
    private String dixonCategoria;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal dixonMonto;

    @Column(name = "entrega", nullable = false, length = 12)
    private String dixonEntrega;

    @Column(name = "publicos", nullable = false, length = 160)
    private String dixonPublicos;

    @Column(name = "plantilla", length = 20)
    private String dixonPlantilla;

    @Column(name = "descripcion", length = 240)
    private String dixonDescripcion;

    @Column(name = "unidad", length = 30)
    private String dixonUnidad;

    @Column(name = "por_cantidad", nullable = false)
    private boolean dixonPorCantidad;

    public List<String> dixonObtenerListaPublicos() {
        return Arrays.asList(dixonPublicos.split("\\|"));
    }

    public boolean dixonAdmite(String dixonTipoSolicitante) {
        return dixonObtenerListaPublicos().contains(dixonTipoSolicitante);
    }

    public boolean dixonEsCarne() {
        return "Carné".equals(dixonCategoria);
    }

    public boolean dixonGeneraDocumento() {
        return dixonPlantilla != null && !dixonPlantilla.isBlank();
    }

    public String dixonObtenerTextoEntrega() {
        return switch (dixonEntrega) {
            case "mismo" -> "Recojo el mismo día";
            case "inmediato" -> "Habilitación inmediata";
            case "carne" -> "10 días hábiles";
            default -> dixonEntrega + " días hábiles";
        };
    }
}
