package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.*;
import pe.andra.repository.DixonMensajeEnviadoRepositorio;
import pe.andra.util.DixonFechas;
import pe.andra.util.DixonHash;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DixonSeguimientoServicio {

    public record DixonEtapa(String dixonIcono, String dixonTitulo) {
    }

    public record DixonSeguimientoCjsv(List<DixonEtapa> dixonEtapas, int dixonActual, List<LocalDateTime> dixonTiempos, String dixonEstimado,
                                  int dixonVentanilla, boolean dixonObservado) {
    }

    public record DixonPreferencias(boolean dixonWhatsapp, boolean dixonCorreo, boolean dixonAplicacion) {
    }

    private final DixonMensajeEnviadoRepositorio dixonMensajes;
    private final DixonAjustesServicio dixonAjustes;

    public static int dixonVentanilla(String dixonCodigo) {
        return 1 + (int) (DixonHash.dixonNumero(dixonCodigo) % 6);
    }

    public static LocalDate dixonFechaDe(DixonExpediente dixonExpediente) {
        if (dixonExpediente.getDixonFecha() != null) {
            return dixonExpediente.getDixonFecha().toLocalDate();
        }
        return dixonExpediente.getDixonDatos().isEmpty() ? null : DixonFechas.dixonDesdeTexto(dixonExpediente.getDixonDatos().get(0).getDixonValor());
    }

    public static DixonSeguimientoCjsv dixonCalcular(DixonExpediente dixonExpediente) {
        DixonPago dixonPago = dixonExpediente.getDixonPago();
        LocalDate dixonDia = dixonFechaDe(dixonExpediente);
        LocalDateTime dixonBase = dixonExpediente.getDixonFecha() != null ? dixonExpediente.getDixonFecha() : (dixonDia != null ? dixonDia : DixonFechas.dixonHoy()).atTime(9, 0);
        int dixonVen = dixonVentanilla(dixonExpediente.getDixonCodigo());
        boolean dixonEntregado = dixonExpediente.dixonEsEntregado();
        DixonGrupoExpediente dixonGrupoExpediente = dixonExpediente.getDixonGrupo();
        List<DixonEtapa> dixonEtapas = new ArrayList<>();
        int dixonActual;
        if (dixonExpediente.getDixonClase().dixonEsReservaOPrestamo()) {
            boolean dixonLab = dixonExpediente.getDixonClase() == DixonClaseExpediente.LAB;
            dixonEtapas.add(new DixonEtapa("fth-inbox", "Solicitado"));
            dixonEtapas.add(new DixonEtapa("fth-check", "Aprobado"));
            dixonEtapas.add(new DixonEtapa(dixonLab ? "fth-key" : "fth-box", dixonLab ? "Listo para usar" : "Listo para retirar"));
            dixonEtapas.add(new DixonEtapa("fth-flag", "Finalizado"));
            dixonActual = dixonGrupoExpediente == DixonGrupoExpediente.ANULADO ? 0 : dixonGrupoExpediente == DixonGrupoExpediente.LISTO ? 2 : 1;
        } else if (dixonPago != null) {
            dixonEtapas.add(new DixonEtapa("fth-inbox", "Recibido"));
            dixonEtapas.add(new DixonEtapa("fth-credit-card", "Pago confirmado"));
            dixonEtapas.add(new DixonEtapa("fth-pen-tool", "En firma"));
            dixonEtapas.add(new DixonEtapa("fth-printer", "Impreso"));
            dixonEtapas.add(new DixonEtapa("fth-map-pin", "Listo en ventanilla " + dixonVen));
            dixonEtapas.add(new DixonEtapa("fth-flag", "Entregado"));
            dixonActual = !dixonPago.dixonEsPagado() ? 1 : dixonEntregado ? 6 : dixonGrupoExpediente == DixonGrupoExpediente.LISTO ? 4 : 2;
        } else {
            dixonEtapas.add(new DixonEtapa("fth-inbox", "Recibido"));
            dixonEtapas.add(new DixonEtapa("fth-eye", "En revisión"));
            dixonEtapas.add(new DixonEtapa("fth-pen-tool", "En firma"));
            dixonEtapas.add(new DixonEtapa("fth-map-pin", "Listo en ventanilla " + dixonVen));
            dixonEtapas.add(new DixonEtapa("fth-flag", "Entregado"));
            dixonActual = dixonEntregado ? 5 : dixonGrupoExpediente == DixonGrupoExpediente.LISTO ? 3 : Math.min(2, Math.max(1, dixonExpediente.getDixonPaso()));
        }
        List<LocalDateTime> dixonTiempos = new ArrayList<>();
        for (int dixonIndice = 0; dixonIndice < dixonEtapas.size(); dixonIndice++) {
            if (dixonIndice == 0) {
                dixonTiempos.add(dixonBase);
            } else if (dixonIndice == 1 && dixonPago != null && dixonPago.getDixonPagado() != null) {
                dixonTiempos.add(dixonPago.getDixonPagado());
            } else {
                LocalDateTime dixonDesde = dixonPago != null && dixonPago.getDixonPagado() != null ? dixonPago.getDixonPagado() : dixonBase;
                dixonTiempos.add(dixonDesde.plusMinutes(dixonIndice * 45L));
            }
        }
        String dixonEstimado;
        if (dixonGrupoExpediente == DixonGrupoExpediente.OBSERVADO) {
            dixonEstimado = "Detenido: tienes una observación por subsanar";
        } else if (dixonGrupoExpediente == DixonGrupoExpediente.ANULADO) {
            dixonEstimado = "Trámite anulado";
        } else if (dixonActual >= dixonEtapas.size()) {
            dixonEstimado = "Entregado";
        } else if (dixonPago != null && !dixonPago.dixonEsPagado()) {
            dixonEstimado = "Esperando tu pago en Andra Pay";
        } else if (dixonPago != null && dixonPago.getDixonRecojoFecha() != null) {
            dixonEstimado = "Listo para recoger: " + dixonPago.getDixonRecojoTexto();
        } else if (dixonGrupoExpediente == DixonGrupoExpediente.LISTO) {
            dixonEstimado = "Listo · Ventanilla " + dixonVen + ", Mesa de Partes · Sede " + dixonExpediente.getDixonSede().getDixonNombre();
        } else {
            dixonEstimado = "Estimado: 2 a 3 días hábiles";
        }
        return new DixonSeguimientoCjsv(dixonEtapas, dixonActual, dixonTiempos, dixonEstimado, dixonVen, dixonGrupoExpediente == DixonGrupoExpediente.OBSERVADO);
    }

    public DixonPreferencias dixonPreferencias() {
        DixonAjustes dixonAjustes2 = dixonAjustes.dixonObtener();
        return new DixonPreferencias(dixonAjustes2.isDixonAvisoWhatsapp(), dixonAjustes2.isDixonAvisoCorreo(), dixonAjustes2.isDixonAvisoAplicacion());
    }

    public void dixonCambiarPreferencia(String dixonCanal, boolean dixonValor) {
        dixonAjustes.dixonCambiar(dixonAjustes2 -> {
            switch (dixonCanal) {
                case "wa" -> dixonAjustes2.setDixonAvisoWhatsapp(dixonValor);
                case "mail" -> dixonAjustes2.setDixonAvisoCorreo(dixonValor);
                default -> dixonAjustes2.setDixonAvisoAplicacion(dixonValor);
            }
        });
    }

    @Transactional
    public int dixonRegistrar(DixonExpediente dixonExpediente, String dixonTexto) {
        if (!dixonExpediente.getDixonPersona().isDixonPrincipal()) {
            return 0;
        }
        DixonPreferencias dixonPref = dixonPreferencias();
        LocalDateTime dixonAhora = DixonFechas.dixonAhora();
        int dixonCantidad = 0;
        if (dixonPref.dixonWhatsapp()) {
            dixonMensajes.save(DixonMensajeEnviado.builder().dixonExpediente(dixonExpediente).dixonCanal(DixonCanalAviso.WHATSAPP).dixonTexto(dixonTexto.trim()).dixonEnviado(dixonAhora).build());
            dixonCantidad++;
        }
        if (dixonPref.dixonCorreo()) {
            dixonMensajes.save(DixonMensajeEnviado.builder().dixonExpediente(dixonExpediente).dixonCanal(DixonCanalAviso.CORREO).dixonTexto(dixonTexto.trim()).dixonEnviado(dixonAhora).build());
            dixonCantidad++;
        }
        return dixonCantidad;
    }

    @Transactional(readOnly = true)
    public List<DixonMensajeEnviado> dixonMensajes(DixonExpediente dixonExpediente) {
        return dixonMensajes.findByDixonExpedienteOrderByDixonIdDesc(dixonExpediente).stream().limit(4).toList();
    }
}
