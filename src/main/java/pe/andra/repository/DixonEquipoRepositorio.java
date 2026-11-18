package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonEquipo;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonEquipoRepositorio extends JpaRepository<DixonEquipo, Long> {

    Optional<DixonEquipo> findByDixonCodigo(String dixonCodigo);

    List<DixonEquipo> findAllByOrderByDixonIdAsc();
}
