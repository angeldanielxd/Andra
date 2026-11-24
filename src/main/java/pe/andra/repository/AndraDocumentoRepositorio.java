package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraDocumento;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraDocumentoRepositorio extends JpaRepository<AndraDocumento, Long> {

    Optional<AndraDocumento> findByAndraCodigo(String andraCodigo);

    List<AndraDocumento> findAllByOrderByAndraIdAsc();
}
