package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraEscuelaProfesional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraEscuelaProfesionalRepositorio extends JpaRepository<AndraEscuelaProfesional, Long> {

    Optional<AndraEscuelaProfesional> findByAndraCodigo(String andraCodigo);

    List<AndraEscuelaProfesional> findAllByOrderByAndraIdAsc();
}
