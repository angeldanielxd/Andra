package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraPrestamoActivo;

import java.util.Optional;

@Repository
public interface AndraPrestamoActivoRepositorio extends JpaRepository<AndraPrestamoActivo, Long> {

    Optional<AndraPrestamoActivo> findFirstByOrderByAndraIdDesc();
}
