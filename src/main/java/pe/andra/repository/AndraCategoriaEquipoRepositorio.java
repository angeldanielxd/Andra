package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraCategoriaEquipo;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraCategoriaEquipoRepositorio extends JpaRepository<AndraCategoriaEquipo, Long> {

    Optional<AndraCategoriaEquipo> findByAndraCodigo(String andraCodigo);

    List<AndraCategoriaEquipo> findAllByOrderByAndraIdAsc();
}
