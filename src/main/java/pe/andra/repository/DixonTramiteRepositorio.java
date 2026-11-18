package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonTramite;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonTramiteRepositorio extends JpaRepository<DixonTramite, Long> {

    Optional<DixonTramite> findByDixonCodigo(String dixonCodigo);

    Optional<DixonTramite> findFirstByDixonNombre(String dixonNombre);

    List<DixonTramite> findAllByOrderByDixonIdAsc();
}
