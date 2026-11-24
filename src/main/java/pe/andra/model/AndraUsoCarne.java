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
@EqualsAndHashCode(of = "andraId")
public class AndraUsoCarne {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "lugar", nullable = false, length = 120)
    private String andraLugar;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime andraFecha;

    @Column(name = "concedido", nullable = false)
    private boolean andraConcedido;
}
