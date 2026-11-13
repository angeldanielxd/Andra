package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.Ajustes;

@Repository
public interface AjustesRepository extends JpaRepository<Ajustes, Long> {
}
