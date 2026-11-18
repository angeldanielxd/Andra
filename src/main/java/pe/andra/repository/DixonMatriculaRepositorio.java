package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonMatricula;

import java.util.Optional;

@Repository
public interface DixonMatriculaRepositorio extends JpaRepository<DixonMatricula, Long> {

    Optional<DixonMatricula> findFirstByOrderByDixonIdDesc();
}
