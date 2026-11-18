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
@EqualsAndHashCode(of = "dixonId")
public class DixonDocumentoPersonal {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @Column(name = "nombre", nullable = false, length = 160)
    private String dixonNombre;
}
