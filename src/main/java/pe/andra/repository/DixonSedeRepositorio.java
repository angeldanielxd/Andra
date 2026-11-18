package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonSede;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonSedeRepositorio extends JpaRepository<DixonSede, Long> {

    Optional<DixonSede> findByDixonCodigo(String dixonCodigo);

    List<DixonSede> findAllByOrderByDixonIdAsc();
}
