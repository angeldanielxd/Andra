package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonPersona;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonPersonaRepositorio extends JpaRepository<DixonPersona, Long> {

    Optional<DixonPersona> findByDixonCodigo(String dixonCodigo);

    Optional<DixonPersona> findFirstByDixonDni(String dixonDni);

    Optional<DixonPersona> findFirstByDixonPrincipalTrue();

    List<DixonPersona> findAllByOrderByDixonIdAsc();
}
