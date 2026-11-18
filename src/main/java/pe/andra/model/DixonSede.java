package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sede", uniqueConstraints = @UniqueConstraint(name = "uk_sede_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonSede {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @Column(name = "nombre", nullable = false, length = 40)
    private String dixonNombre;

    @Column(name = "nombre_completo", nullable = false, length = 60)
    private String dixonNombreCompleto;

    @Column(name = "descripcion", length = 200)
    private String dixonDescripcion;

    @Column(name = "oficina", length = 120)
    private String dixonOficina;

    @Column(name = "horario", length = 120)
    private String dixonHorario;
}
