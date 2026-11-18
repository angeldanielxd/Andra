package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "espacio", uniqueConstraints = @UniqueConstraint(name = "uk_espacio_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonEspacio {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 30)
    private String dixonCodigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String dixonNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 15)
    private DixonTipoEspacio dixonTipo;

    @Column(name = "capacidad", nullable = false)
    private int dixonCapacidad;

    @Column(name = "fuera_de_servicio", nullable = false)
    private boolean dixonFueraDeServicio;

    @Column(name = "edificio", length = 80)
    private String dixonEdificio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private DixonSede dixonSede;

    @ManyToOne
    @JoinColumn(name = "escuela_id")
    private DixonEscuelaProfesional dixonEscuela;

    public String dixonObtenerPabellon() {
        return dixonEdificio == null ? "" : dixonEdificio.split(",")[0].trim();
    }
}
