package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonPago;
import pe.andra.model.DixonPersona;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonPagoRepositorio extends JpaRepository<DixonPago, Long> {

    Optional<DixonPago> findByDixonCodigo(String dixonCodigo);

    List<DixonPago> findAllByOrderByDixonCreadoDesc();

    List<DixonPago> findByDixonPersonaOrderByDixonCreadoDesc(DixonPersona dixonPersona);

    long countByDixonBoletaIsNotNull();
}
