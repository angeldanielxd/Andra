package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraSede;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraSedeRepositorio extends JpaRepository<AndraSede, Long> {

    Optional<AndraSede> findByAndraCodigo(String andraCodigo);

    List<AndraSede> findAllByOrderByAndraIdAsc();
}
