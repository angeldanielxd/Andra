package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonExpediente;
import pe.andra.model.DixonPago;
import pe.andra.model.DixonPersona;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonExpedienteRepositorio extends JpaRepository<DixonExpediente, Long> {

    Optional<DixonExpediente> findByDixonCodigo(String dixonCodigo);

    Optional<DixonExpediente> findFirstByDixonPago(DixonPago dixonPago);

    List<DixonExpediente> findAllByOrderByDixonIdDesc();

    List<DixonExpediente> findByDixonPersonaOrderByDixonIdDesc(DixonPersona dixonPersona);

    boolean existsByDixonCodigo(String dixonCodigo);
}
