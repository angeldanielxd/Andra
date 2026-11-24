package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraAjustes;

@Repository
public interface AndraAjustesRepositorio extends JpaRepository<AndraAjustes, Long> {
}
