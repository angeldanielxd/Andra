package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prestamo", uniqueConstraints = @UniqueConstraint(name = "uk_prestamo_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraPrestamo {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @Column(name = "clave", nullable = false, length = 20)
    private String andraClave;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private AndraPersona andraPersona;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private AndraEquipo andraEquipo;

    @Column(name = "nombre", nullable = false, length = 160)
    private String andraNombre;

    @Column(name = "detalle", nullable = false, length = 240)
    private String andraDetalle;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private AndraEstadoPrestamo andraEstado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private AndraSede andraSede;
}
