package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonEspacio;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonEspacioRepositorio extends JpaRepository<DixonEspacio, Long> {

    Optional<DixonEspacio> findByDixonCodigo(String dixonCodigo);

    Optional<DixonEspacio> findFirstByDixonNombre(String dixonNombre);

    List<DixonEspacio> findAllByOrderByDixonIdAsc();
}
