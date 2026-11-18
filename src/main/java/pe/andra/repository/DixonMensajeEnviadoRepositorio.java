package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonExpediente;
import pe.andra.model.DixonMensajeEnviado;

import java.util.List;

@Repository
public interface DixonMensajeEnviadoRepositorio extends JpaRepository<DixonMensajeEnviado, Long> {

    List<DixonMensajeEnviado> findByDixonExpedienteOrderByDixonIdDesc(DixonExpediente dixonExpediente);
}
