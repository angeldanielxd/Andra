package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonPrestamoActivo;

import java.util.Optional;

@Repository
public interface DixonPrestamoActivoRepositorio extends JpaRepository<DixonPrestamoActivo, Long> {

    Optional<DixonPrestamoActivo> findFirstByOrderByDixonIdDesc();
}
