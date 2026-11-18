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
@EqualsAndHashCode(of = "dixonId")
public class DixonPrestamo {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @Column(name = "clave", nullable = false, length = 20)
    private String dixonClave;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private DixonPersona dixonPersona;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private DixonEquipo dixonEquipo;

    @Column(name = "nombre", nullable = false, length = 160)
    private String dixonNombre;

    @Column(name = "detalle", nullable = false, length = 240)
    private String dixonDetalle;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private DixonEstadoPrestamo dixonEstado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private DixonSede dixonSede;
}
