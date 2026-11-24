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
@EqualsAndHashCode(of = "andraId")
public class AndraEspacio {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 30)
    private String andraCodigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String andraNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 15)
    private AndraTipoEspacio andraTipo;

    @Column(name = "capacidad", nullable = false)
    private int andraCapacidad;

    @Column(name = "fuera_de_servicio", nullable = false)
    private boolean andraFueraDeServicio;

    @Column(name = "edificio", length = 80)
    private String andraEdificio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private AndraSede andraSede;

    @ManyToOne
    @JoinColumn(name = "escuela_id")
    private AndraEscuelaProfesional andraEscuela;

    public String andraObtenerPabellon() {
        return andraEdificio == null ? "" : andraEdificio.split(",")[0].trim();
    }
}
