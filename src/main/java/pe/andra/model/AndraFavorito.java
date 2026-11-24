package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorito", uniqueConstraints = @UniqueConstraint(name = "uk_favorito", columnNames = {"tipo", "codigo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraFavorito {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 14)
    private AndraTipoFavorito andraTipo;

    @Column(name = "codigo", nullable = false, length = 30)
    private String andraCodigo;
}
