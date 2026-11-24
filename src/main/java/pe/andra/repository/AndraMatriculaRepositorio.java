package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraMatricula;

import java.util.Optional;

@Repository
public interface AndraMatriculaRepositorio extends JpaRepository<AndraMatricula, Long> {

    Optional<AndraMatricula> findFirstByOrderByAndraIdDesc();
}
