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
@EqualsAndHashCode(of = "dixonId")
public class DixonPago {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dixonId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String dixonCodigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private DixonPersona dixonPersona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 12)
    private DixonTipoPago dixonTipo;

    @ManyToOne
    @JoinColumn(name = "tramite_id")
    private DixonTramite dixonTramite;

    @Column(name = "concepto", nullable = false, length = 160)
    private String dixonConcepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private DixonEstadoPago dixonEstado;

    @Column(name = "creado", nullable = false)
    private LocalDateTime dixonCreado;

    @Column(name = "vence")
    private LocalDate dixonVence;

    @Column(name = "pagado")
    private LocalDateTime dixonPagado;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", length = 12)
    private DixonMetodoPago dixonMetodo;

    @Column(name = "operacion", length = 20)
    private String dixonOperacion;

    @Column(name = "boleta", length = 20)
    private String dixonBoleta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private DixonSede dixonSede;

    @Embedded
    private DixonDatosSolicitante dixonSolicitante;

    @Column(name = "motivo", length = 60)
    private String dixonMotivo;

    @Column(name = "foto", columnDefinition = "BLOB")
    private byte[] dixonFoto;

    @Column(name = "recojo_fecha")
    private LocalDate dixonRecojoFecha;

    @Column(name = "recojo_texto", length = 160)
    private String dixonRecojoTexto;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pago_item", joinColumns = @JoinColumn(name = "pago_id"))
    @OrderColumn(name = "orden")
    private List<DixonElementoPago> dixonElementos = new ArrayList<>();

    public BigDecimal dixonObtenerTotal() {
        return dixonElementos.stream().map(DixonElementoPago::dixonObtenerImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean dixonEsPendiente() {
        return dixonEstado == DixonEstadoPago.PENDIENTE;
    }

    public boolean dixonEsPagado() {
        return dixonEstado == DixonEstadoPago.PAGADO;
    }
}
