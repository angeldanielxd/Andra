package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.ConsultaAsesor;
import pe.andra.model.Expediente;

import java.util.Optional;

@Repository
public interface ConsultaAsesorRepository extends JpaRepository<ConsultaAsesor, Long> {

    Optional<ConsultaAsesor> findByExpediente(Expediente expediente);
}
