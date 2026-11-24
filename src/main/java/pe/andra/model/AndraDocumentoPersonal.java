package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documento_personal", uniqueConstraints = @UniqueConstraint(name = "uk_docpersonal_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraDocumentoPersonal {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @Column(name = "nombre", nullable = false, length = 160)
    private String andraNombre;
}
