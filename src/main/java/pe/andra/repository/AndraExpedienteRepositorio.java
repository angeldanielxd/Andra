package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraExpediente;
import pe.andra.model.AndraPago;
import pe.andra.model.AndraPersona;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraExpedienteRepositorio extends JpaRepository<AndraExpediente, Long> {

    Optional<AndraExpediente> findByAndraCodigo(String andraCodigo);

    Optional<AndraExpediente> findFirstByAndraPago(AndraPago andraPago);

    List<AndraExpediente> findAllByOrderByAndraIdDesc();

    List<AndraExpediente> findByAndraPersonaOrderByAndraIdDesc(AndraPersona andraPersona);

    boolean existsByAndraCodigo(String andraCodigo);
}
