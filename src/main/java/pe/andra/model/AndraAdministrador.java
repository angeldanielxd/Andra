package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administrador", uniqueConstraints = @UniqueConstraint(name = "uk_admin_correo", columnNames = "correo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraAdministrador {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String andraNombre;

    @Column(name = "correo", nullable = false, length = 80)
    private String andraCorreo;

    @Column(name = "dni", length = 8)
    private String andraDni;

    @Column(name = "cargo", length = 120)
    private String andraCargo;

    public String andraObtenerPrimerNombre() {
        return andraNombre.split(" ")[0];
    }
}
