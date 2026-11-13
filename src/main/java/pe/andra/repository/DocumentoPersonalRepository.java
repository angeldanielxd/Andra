package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DocumentoPersonal;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoPersonalRepository extends JpaRepository<DocumentoPersonal, Long> {

    Optional<DocumentoPersonal> findByCodigo(String codigo);

    List<DocumentoPersonal> findAllByOrderByIdAsc();
}
