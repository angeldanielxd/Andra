package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria_equipo", uniqueConstraints = @UniqueConstraint(name = "uk_categoria_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraCategoriaEquipo {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @Column(name = "nombre", nullable = false, length = 60)
    private String andraNombre;

    @Column(name = "descripcion", length = 120)
    private String andraDescripcion;

    @Column(name = "icono", nullable = false, length = 30)
    private String andraIcono;
}
