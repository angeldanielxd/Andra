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
@EqualsAndHashCode(of = "dixonId")
public class DixonAdministrador {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String dixonNombre;

    @Column(name = "correo", nullable = false, length = 80)
    private String dixonCorreo;

    @Column(name = "dni", length = 8)
    private String dixonDni;

    @Column(name = "cargo", length = 120)
    private String dixonCargo;

    public String dixonObtenerPrimerNombre() {
        return dixonNombre.split(" ")[0];
    }
}
