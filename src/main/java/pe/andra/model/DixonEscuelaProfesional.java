package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "escuela_profesional", uniqueConstraints = @UniqueConstraint(name = "uk_escuela_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonEscuelaProfesional {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 10)
    private String dixonCodigo;

    @Column(name = "nombre", nullable = false, length = 80)
    private String dixonNombre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private DixonSede dixonSede;
}
