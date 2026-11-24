package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraConsultaAsesor;
import pe.andra.model.AndraExpediente;

import java.util.Optional;

@Repository
public interface AndraConsultaAsesorRepositorio extends JpaRepository<AndraConsultaAsesor, Long> {

    Optional<AndraConsultaAsesor> findByAndraExpediente(AndraExpediente andraExpediente);
}
