package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraAdministrador;

import java.util.Optional;

@Repository
public interface AndraAdministradorRepositorio extends JpaRepository<AndraAdministrador, Long> {

    Optional<AndraAdministrador> findByAndraCorreo(String andraCorreo);

    Optional<AndraAdministrador> findFirstByOrderByAndraIdAsc();
}
