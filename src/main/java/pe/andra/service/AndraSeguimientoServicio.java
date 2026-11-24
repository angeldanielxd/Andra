package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.*;
import pe.andra.repository.AndraMensajeEnviadoRepositorio;
import pe.andra.util.AndraFechas;
import pe.andra.util.AndraHash;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AndraSeguimientoServicio {

    public record AndraEtapa(String andraIcono, String andraTitulo) {
    }

    public record AndraSeguimientoCjsv(List<AndraEtapa> andraEtapas, int andraActual, List<LocalDateTime> andraTiempos, String andraEstimado,
                                  int andraVentanilla, boolean andraObservado) {
    }

    public record AndraPreferencias(boolean andraWhatsapp, boolean andraCorreo, boolean andraAplicacion) {
    }

    private final AndraMensajeEnviadoRepositorio andraMensajes;
    private final AndraAjustesServicio andraAjustes;

    public static int andraVentanilla(String andraCodigo) {
        return 1 + (int) (AndraHash.andraNumero(andraCodigo) % 6);
    }

    public static LocalDate andraFechaDe(AndraExpediente andraExpediente) {
        if (andraExpediente.getAndraFecha() != null) {
            return andraExpediente.getAndraFecha().toLocalDate();
        }
        return andraExpediente.getAndraDatos().isEmpty() ? null : AndraFechas.andraDesdeTexto(andraExpediente.getAndraDatos().get(0).getAndraValor());
    }

    public static AndraSeguimientoCjsv andraCalcular(AndraExpediente andraExpediente) {
        AndraPago andraPago = andraExpediente.getAndraPago();
        LocalDate andraDia = andraFechaDe(andraExpediente);
        LocalDateTime andraBase = andraExpediente.getAndraFecha() != null ? andraExpediente.getAndraFecha() : (andraDia != null ? andraDia : AndraFechas.andraHoy()).atTime(9, 0);
        int andraVen = andraVentanilla(andraExpediente.getAndraCodigo());
        boolean andraEntregado = andraExpediente.andraEsEntregado();
        AndraGrupoExpediente andraGrupoExpediente = andraExpediente.getAndraGrupo();
        List<AndraEtapa> andraEtapas = new ArrayList<>();
        int andraActual;
        if (andraExpediente.getAndraClase().andraEsReservaOPrestamo()) {
            boolean andraLab = andraExpediente.getAndraClase() == AndraClaseExpediente.LAB;
            andraEtapas.add(new AndraEtapa("fth-inbox", "Solicitado"));
            andraEtapas.add(new AndraEtapa("fth-check", "Aprobado"));
            andraEtapas.add(new AndraEtapa(andraLab ? "fth-key" : "fth-box", andraLab ? "Listo para usar" : "Listo para retirar"));
            andraEtapas.add(new AndraEtapa("fth-flag", "Finalizado"));
            andraActual = andraGrupoExpediente == AndraGrupoExpediente.ANULADO ? 0 : andraGrupoExpediente == AndraGrupoExpediente.LISTO ? 2 : 1;
        } else if (andraPago != null) {
            andraEtapas.add(new AndraEtapa("fth-inbox", "Recibido"));
            andraEtapas.add(new AndraEtapa("fth-credit-card", "Pago confirmado"));
            andraEtapas.add(new AndraEtapa("fth-pen-tool", "En firma"));
            andraEtapas.add(new AndraEtapa("fth-printer", "Impreso"));
            andraEtapas.add(new AndraEtapa("fth-map-pin", "Listo en ventanilla " + andraVen));
            andraEtapas.add(new AndraEtapa("fth-flag", "Entregado"));
            andraActual = !andraPago.andraEsPagado() ? 1 : andraEntregado ? 6 : andraGrupoExpediente == AndraGrupoExpediente.LISTO ? 4 : 2;
        } else {
            andraEtapas.add(new AndraEtapa("fth-inbox", "Recibido"));
            andraEtapas.add(new AndraEtapa("fth-eye", "En revisión"));
            andraEtapas.add(new AndraEtapa("fth-pen-tool", "En firma"));
            andraEtapas.add(new AndraEtapa("fth-map-pin", "Listo en ventanilla " + andraVen));
            andraEtapas.add(new AndraEtapa("fth-flag", "Entregado"));
            andraActual = andraEntregado ? 5 : andraGrupoExpediente == AndraGrupoExpediente.LISTO ? 3 : Math.min(2, Math.max(1, andraExpediente.getAndraPaso()));
        }
        List<LocalDateTime> andraTiempos = new ArrayList<>();
        for (int andraIndice = 0; andraIndice < andraEtapas.size(); andraIndice++) {
            if (andraIndice == 0) {
                andraTiempos.add(andraBase);
            } else if (andraIndice == 1 && andraPago != null && andraPago.getAndraPagado() != null) {
                andraTiempos.add(andraPago.getAndraPagado());
            } else {
                LocalDateTime andraDesde = andraPago != null && andraPago.getAndraPagado() != null ? andraPago.getAndraPagado() : andraBase;
                andraTiempos.add(andraDesde.plusMinutes(andraIndice * 45L));
            }
        }
        String andraEstimado;
        if (andraGrupoExpediente == AndraGrupoExpediente.OBSERVADO) {
            andraEstimado = "Detenido: tienes una observación por subsanar";
        } else if (andraGrupoExpediente == AndraGrupoExpediente.ANULADO) {
            andraEstimado = "Trámite anulado";
        } else if (andraActual >= andraEtapas.size()) {
            andraEstimado = "Entregado";
        } else if (andraPago != null && !andraPago.andraEsPagado()) {
            andraEstimado = "Esperando tu pago en Andra Pay";
        } else if (andraPago != null && andraPago.getAndraRecojoFecha() != null) {
            andraEstimado = "Listo para recoger: " + andraPago.getAndraRecojoTexto();
        } else if (andraGrupoExpediente == AndraGrupoExpediente.LISTO) {
            andraEstimado = "Listo · Ventanilla " + andraVen + ", Mesa de Partes · Sede " + andraExpediente.getAndraSede().getAndraNombre();
        } else {
            andraEstimado = "Estimado: 2 a 3 días hábiles";
        }
        return new AndraSeguimientoCjsv(andraEtapas, andraActual, andraTiempos, andraEstimado, andraVen, andraGrupoExpediente == AndraGrupoExpediente.OBSERVADO);
    }

    public AndraPreferencias andraPreferencias() {
        AndraAjustes andraAjustes2 = andraAjustes.andraObtener();
        return new AndraPreferencias(andraAjustes2.isAndraAvisoWhatsapp(), andraAjustes2.isAndraAvisoCorreo(), andraAjustes2.isAndraAvisoAplicacion());
    }

    public void andraCambiarPreferencia(String andraCanal, boolean andraValor) {
        andraAjustes.andraCambiar(andraAjustes2 -> {
            switch (andraCanal) {
                case "wa" -> andraAjustes2.setAndraAvisoWhatsapp(andraValor);
                case "mail" -> andraAjustes2.setAndraAvisoCorreo(andraValor);
                default -> andraAjustes2.setAndraAvisoAplicacion(andraValor);
            }
        });
    }

    @Transactional
    public int andraRegistrar(AndraExpediente andraExpediente, String andraTexto) {
        if (!andraExpediente.getAndraPersona().isAndraPrincipal()) {
            return 0;
        }
        AndraPreferencias andraPref = andraPreferencias();
        LocalDateTime andraAhora = AndraFechas.andraAhora();
        int andraCantidad = 0;
        if (andraPref.andraWhatsapp()) {
            andraMensajes.save(AndraMensajeEnviado.builder().andraExpediente(andraExpediente).andraCanal(AndraCanalAviso.WHATSAPP).andraTexto(andraTexto.trim()).andraEnviado(andraAhora).build());
            andraCantidad++;
        }
        if (andraPref.andraCorreo()) {
            andraMensajes.save(AndraMensajeEnviado.builder().andraExpediente(andraExpediente).andraCanal(AndraCanalAviso.CORREO).andraTexto(andraTexto.trim()).andraEnviado(andraAhora).build());
            andraCantidad++;
        }
        return andraCantidad;
    }

    @Transactional(readOnly = true)
    public List<AndraMensajeEnviado> andraMensajes(AndraExpediente andraExpediente) {
        return andraMensajes.findByAndraExpedienteOrderByAndraIdDesc(andraExpediente).stream().limit(4).toList();
    }
}
