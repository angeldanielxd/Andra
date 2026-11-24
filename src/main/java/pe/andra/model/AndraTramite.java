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
@EqualsAndHashCode(of = "andraId")
public class AndraTramite {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String andraNombre;

    @Column(name = "categoria", nullable = false, length = 40)
    private String andraCategoria;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal andraMonto;

    @Column(name = "entrega", nullable = false, length = 12)
    private String andraEntrega;

    @Column(name = "publicos", nullable = false, length = 160)
    private String andraPublicos;

    @Column(name = "plantilla", length = 20)
    private String andraPlantilla;

    @Column(name = "descripcion", length = 240)
    private String andraDescripcion;

    @Column(name = "unidad", length = 30)
    private String andraUnidad;

    @Column(name = "por_cantidad", nullable = false)
    private boolean andraPorCantidad;

    public List<String> andraObtenerListaPublicos() {
        return Arrays.asList(andraPublicos.split("\\|"));
    }

    public boolean andraAdmite(String andraTipoSolicitante) {
        return andraObtenerListaPublicos().contains(andraTipoSolicitante);
    }

    public boolean andraEsCarne() {
        return "Carné".equals(andraCategoria);
    }

    public boolean andraGeneraDocumento() {
        return andraPlantilla != null && !andraPlantilla.isBlank();
    }

    public String andraObtenerTextoEntrega() {
        return switch (andraEntrega) {
            case "mismo" -> "Recojo el mismo día";
            case "inmediato" -> "Habilitación inmediata";
            case "carne" -> "10 días hábiles";
            default -> andraEntrega + " días hábiles";
        };
    }
}
