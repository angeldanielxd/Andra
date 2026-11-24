package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.AndraNotificacion;
import pe.andra.model.AndraRol;
import pe.andra.repository.AndraNotificacionRepositorio;
import pe.andra.util.AndraFechas;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AndraNotificacionServicio {

    private final AndraNotificacionRepositorio andraRepositorio;
    private final ApplicationEventPublisher andraEventos;

    @Transactional(readOnly = true)
    public List<AndraNotificacion> andraListar(AndraRol andraRol) {
        return andraRepositorio.findByAndraDestinatarioOrderByAndraIdDesc(andraRol);
    }

    @Transactional(readOnly = true)
    public long andraNoLeidas(AndraRol andraRol) {
        return andraRol == null ? 0 : andraRepositorio.countByAndraDestinatarioAndAndraLeidaFalse(andraRol);
    }

    @Transactional
    public void andraMarcarTodas(AndraRol andraRol) {
        List<AndraNotificacion> andraLista = andraRepositorio.findByAndraDestinatarioOrderByAndraIdDesc(andraRol);
        andraLista.forEach(andraNotificacion -> andraNotificacion.setAndraLeida(true));
        andraRepositorio.saveAll(andraLista);
        andraEventos.publishEvent(new AndraCambioDatos("notificaciones"));
    }

    @Transactional
    public String andraAbrir(Long andraId) {
        AndraNotificacion andraNotificacion = andraRepositorio.findById(andraId).orElseThrow();
        andraNotificacion.setAndraLeida(true);
        andraRepositorio.save(andraNotificacion);
        andraEventos.publishEvent(new AndraCambioDatos("notificaciones"));
        return andraNotificacion.getAndraRuta();
    }

    @Transactional
    public void andraNotificarEstudiante(String andraTexto, String andraRuta) {
        andraGuardar(AndraRol.ESTUDIANTE, andraTexto, andraRuta);
    }

    @Transactional
    public void andraNotificarAdministrador(String andraTexto, String andraRuta) {
        andraGuardar(AndraRol.ADMINISTRADOR, andraTexto, andraRuta);
    }

    @Transactional
    public void andraQuitarPorTexto(String andraFragmento) {
        andraRepositorio.findByAndraDestinatarioOrderByAndraIdDesc(AndraRol.ESTUDIANTE).stream()
                .filter(andraNotificacion -> andraNotificacion.getAndraTexto().contains(andraFragmento)).forEach(andraRepositorio::delete);
        andraEventos.publishEvent(new AndraCambioDatos("notificaciones"));
    }

    private void andraGuardar(AndraRol andraRol, String andraTexto, String andraRuta) {
        andraRepositorio.save(AndraNotificacion.builder().andraDestinatario(andraRol).andraTexto(andraTexto).andraRuta(andraRuta).andraLeida(false)
                .andraCreada(AndraFechas.andraAhora()).build());
        andraEventos.publishEvent(new AndraCambioDatos("notificaciones"));
    }
}
