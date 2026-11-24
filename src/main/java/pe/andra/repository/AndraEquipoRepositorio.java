package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraEquipo;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraEquipoRepositorio extends JpaRepository<AndraEquipo, Long> {

    Optional<AndraEquipo> findByAndraCodigo(String andraCodigo);

    List<AndraEquipo> findAllByOrderByAndraIdAsc();
}
