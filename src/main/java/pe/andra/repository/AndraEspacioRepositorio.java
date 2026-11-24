package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraEspacio;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraEspacioRepositorio extends JpaRepository<AndraEspacio, Long> {

    Optional<AndraEspacio> findByAndraCodigo(String andraCodigo);

    Optional<AndraEspacio> findFirstByAndraNombre(String andraNombre);

    List<AndraEspacio> findAllByOrderByAndraIdAsc();
}
