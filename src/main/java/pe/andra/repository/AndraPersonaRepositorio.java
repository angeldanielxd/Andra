package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraPersona;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraPersonaRepositorio extends JpaRepository<AndraPersona, Long> {

    Optional<AndraPersona> findByAndraCodigo(String andraCodigo);

    Optional<AndraPersona> findFirstByAndraDni(String andraDni);

    Optional<AndraPersona> findFirstByAndraPrincipalTrue();

    List<AndraPersona> findAllByOrderByAndraIdAsc();
}
