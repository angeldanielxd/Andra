package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonPersona;
import pe.andra.model.DixonPrestamo;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonPrestamoRepositorio extends JpaRepository<DixonPrestamo, Long> {

    Optional<DixonPrestamo> findByDixonClave(String dixonClave);

    List<DixonPrestamo> findAllByOrderByDixonIdDesc();

    List<DixonPrestamo> findByDixonPersonaOrderByDixonIdDesc(DixonPersona dixonPersona);

    boolean existsByDixonCodigo(String dixonCodigo);
}
