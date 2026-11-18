package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cambio_pendiente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonCambioPendiente {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "texto", nullable = false, length = 200)
    private String dixonTexto;

    @Column(name = "creado", nullable = false)
    private LocalDateTime dixonCreado;
}
