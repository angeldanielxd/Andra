package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "uso_carne")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "dixonId")
public class DixonUsoCarne {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "lugar", nullable = false, length = 120)
    private String dixonLugar;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime dixonFecha;

    @Column(name = "concedido", nullable = false)
    private boolean dixonConcedido;
}
