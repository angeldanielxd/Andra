package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraNotificacion;
import pe.andra.model.AndraRol;

import java.util.List;

@Repository
public interface AndraNotificacionRepositorio extends JpaRepository<AndraNotificacion, Long> {

    List<AndraNotificacion> findByAndraDestinatarioOrderByAndraIdDesc(AndraRol andraDestinatario);

    long countByAndraDestinatarioAndAndraLeidaFalse(AndraRol andraDestinatario);
}
