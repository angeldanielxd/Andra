package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraExpediente;
import pe.andra.model.AndraMensajeEnviado;

import java.util.List;

@Repository
public interface AndraMensajeEnviadoRepositorio extends JpaRepository<AndraMensajeEnviado, Long> {

    List<AndraMensajeEnviado> findByAndraExpedienteOrderByAndraIdDesc(AndraExpediente andraExpediente);
}
