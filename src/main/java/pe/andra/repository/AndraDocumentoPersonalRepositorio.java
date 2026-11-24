package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraDocumentoPersonal;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraDocumentoPersonalRepositorio extends JpaRepository<AndraDocumentoPersonal, Long> {

    Optional<AndraDocumentoPersonal> findByAndraCodigo(String andraCodigo);

    List<AndraDocumentoPersonal> findAllByOrderByAndraIdAsc();
}
