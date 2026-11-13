package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.Expediente;
import pe.andra.model.MensajeEnviado;

import java.util.List;

@Repository
public interface MensajeEnviadoRepository extends JpaRepository<MensajeEnviado, Long> {

    List<MensajeEnviado> findByExpedienteOrderByIdDesc(Expediente expediente);
}
