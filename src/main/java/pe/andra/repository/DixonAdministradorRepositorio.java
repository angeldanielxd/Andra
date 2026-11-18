package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonAdministrador;

import java.util.Optional;

@Repository
public interface DixonAdministradorRepositorio extends JpaRepository<DixonAdministrador, Long> {

    Optional<DixonAdministrador> findByDixonCorreo(String dixonCorreo);

    Optional<DixonAdministrador> findFirstByOrderByDixonIdAsc();
}
