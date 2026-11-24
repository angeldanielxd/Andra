package pe.andra.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pago", uniqueConstraints = @UniqueConstraint(name = "uk_pago_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "andraId")
public class AndraPago {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long andraId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String andraCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private AndraPersona andraPersona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 12)
    private AndraTipoPago andraTipo;

    @ManyToOne
    @JoinColumn(name = "tramite_id")
    private AndraTramite andraTramite;

    @Column(name = "concepto", nullable = false, length = 160)
    private String andraConcepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private AndraEstadoPago andraEstado;

    @Column(name = "creado", nullable = false)
    private LocalDateTime andraCreado;

    @Column(name = "vence")
    private LocalDate andraVence;

    @Column(name = "pagado")
    private LocalDateTime andraPagado;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", length = 12)
    private AndraMetodoPago andraMetodo;

    @Column(name = "operacion", length = 20)
    private String andraOperacion;

    @Column(name = "boleta", length = 20)
    private String andraBoleta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private AndraSede andraSede;

    @Embedded
    private AndraDatosSolicitante andraSolicitante;

    @Column(name = "motivo", length = 60)
    private String andraMotivo;

    @Column(name = "foto", columnDefinition = "BLOB")
    private byte[] andraFoto;

    @Column(name = "recojo_fecha")
    private LocalDate andraRecojoFecha;

    @Column(name = "recojo_texto", length = 160)
    private String andraRecojoTexto;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pago_item", joinColumns = @JoinColumn(name = "pago_id"))
    @OrderColumn(name = "orden")
    private List<AndraElementoPago> andraElementos = new ArrayList<>();

    public BigDecimal andraObtenerTotal() {
        return andraElementos.stream().map(AndraElementoPago::andraObtenerImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean andraEsPendiente() {
        return andraEstado == AndraEstadoPago.PENDIENTE;
    }

    public boolean andraEsPagado() {
        return andraEstado == AndraEstadoPago.PAGADO;
    }
}
