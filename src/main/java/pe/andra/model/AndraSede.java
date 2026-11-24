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
@EqualsAndHashCode(of = "andraId")
public class AndraSede {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @Column(name = "nombre", nullable = false, length = 40)
    private String andraNombre;

    @Column(name = "nombre_completo", nullable = false, length = 60)
    private String andraNombreCompleto;

    @Column(name = "descripcion", length = 200)
    private String andraDescripcion;

    @Column(name = "oficina", length = 120)
    private String andraOficina;

    @Column(name = "horario", length = 120)
    private String andraHorario;
}
