package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraFavorito;
import pe.andra.model.AndraTipoFavorito;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraFavoritoRepositorio extends JpaRepository<AndraFavorito, Long> {

    List<AndraFavorito> findByAndraTipoOrderByAndraIdAsc(AndraTipoFavorito andraTipo);

    Optional<AndraFavorito> findByAndraTipoAndAndraCodigo(AndraTipoFavorito andraTipo, String andraCodigo);

    boolean existsByAndraTipoAndAndraCodigo(AndraTipoFavorito andraTipo, String andraCodigo);
}
