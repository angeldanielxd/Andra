package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.PrestamoActivo;

import java.util.Optional;

@Repository
public interface PrestamoActivoRepository extends JpaRepository<PrestamoActivo, Long> {

    Optional<PrestamoActivo> findFirstByOrderByIdDesc();
}
