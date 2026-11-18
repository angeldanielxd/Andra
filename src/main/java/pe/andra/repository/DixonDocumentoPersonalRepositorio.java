package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonDocumentoPersonal;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonDocumentoPersonalRepositorio extends JpaRepository<DixonDocumentoPersonal, Long> {

    Optional<DixonDocumentoPersonal> findByDixonCodigo(String dixonCodigo);

    List<DixonDocumentoPersonal> findAllByOrderByDixonIdAsc();
}
