package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonCambioPendiente;

import java.util.List;

@Repository
public interface DixonCambioPendienteRepositorio extends JpaRepository<DixonCambioPendiente, Long> {

    List<DixonCambioPendiente> findAllByOrderByDixonIdAsc();
}
