package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonAjustes;

@Repository
public interface DixonAjustesRepositorio extends JpaRepository<DixonAjustes, Long> {
}
