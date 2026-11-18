package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonConsultaAsesor;
import pe.andra.model.DixonExpediente;

import java.util.Optional;

@Repository
public interface DixonConsultaAsesorRepositorio extends JpaRepository<DixonConsultaAsesor, Long> {

    Optional<DixonConsultaAsesor> findByDixonExpediente(DixonExpediente dixonExpediente);
}
