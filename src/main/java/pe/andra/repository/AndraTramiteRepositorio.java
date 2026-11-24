package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraTramite;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraTramiteRepositorio extends JpaRepository<AndraTramite, Long> {

    Optional<AndraTramite> findByAndraCodigo(String andraCodigo);

    Optional<AndraTramite> findFirstByAndraNombre(String andraNombre);

    List<AndraTramite> findAllByOrderByAndraIdAsc();
}
