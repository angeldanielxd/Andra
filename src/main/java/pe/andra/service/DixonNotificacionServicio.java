package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.DixonNotificacion;
import pe.andra.model.DixonRol;
import pe.andra.repository.DixonNotificacionRepositorio;
import pe.andra.util.DixonFechas;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DixonNotificacionServicio {

    private final DixonNotificacionRepositorio dixonRepositorio;
    private final ApplicationEventPublisher dixonEventos;

    @Transactional(readOnly = true)
    public List<DixonNotificacion> dixonListar(DixonRol dixonRol) {
        return dixonRepositorio.findByDixonDestinatarioOrderByDixonIdDesc(dixonRol);
    }

    @Transactional(readOnly = true)
    public long dixonNoLeidas(DixonRol dixonRol) {
        return dixonRol == null ? 0 : dixonRepositorio.countByDixonDestinatarioAndDixonLeidaFalse(dixonRol);
    }

    @Transactional
    public void dixonMarcarTodas(DixonRol dixonRol) {
        List<DixonNotificacion> dixonLista = dixonRepositorio.findByDixonDestinatarioOrderByDixonIdDesc(dixonRol);
        dixonLista.forEach(dixonNotificacion -> dixonNotificacion.setDixonLeida(true));
        dixonRepositorio.saveAll(dixonLista);
        dixonEventos.publishEvent(new DixonCambioDatos("notificaciones"));
    }

    @Transactional
    public String dixonAbrir(Long dixonId) {
        DixonNotificacion dixonNotificacion = dixonRepositorio.findById(dixonId).orElseThrow();
        dixonNotificacion.setDixonLeida(true);
        dixonRepositorio.save(dixonNotificacion);
        dixonEventos.publishEvent(new DixonCambioDatos("notificaciones"));
        return dixonNotificacion.getDixonRuta();
    }

    @Transactional
    public void dixonNotificarEstudiante(String dixonTexto, String dixonRuta) {
        dixonGuardar(DixonRol.ESTUDIANTE, dixonTexto, dixonRuta);
    }

    @Transactional
    public void dixonNotificarAdministrador(String dixonTexto, String dixonRuta) {
        dixonGuardar(DixonRol.ADMINISTRADOR, dixonTexto, dixonRuta);
    }

    @Transactional
    public void dixonQuitarPorTexto(String dixonFragmento) {
        dixonRepositorio.findByDixonDestinatarioOrderByDixonIdDesc(DixonRol.ESTUDIANTE).stream()
                .filter(dixonNotificacion -> dixonNotificacion.getDixonTexto().contains(dixonFragmento)).forEach(dixonRepositorio::delete);
        dixonEventos.publishEvent(new DixonCambioDatos("notificaciones"));
    }

    private void dixonGuardar(DixonRol dixonRol, String dixonTexto, String dixonRuta) {
        dixonRepositorio.save(DixonNotificacion.builder().dixonDestinatario(dixonRol).dixonTexto(dixonTexto).dixonRuta(dixonRuta).dixonLeida(false)
                .dixonCreada(DixonFechas.dixonAhora()).build());
        dixonEventos.publishEvent(new DixonCambioDatos("notificaciones"));
    }
}
