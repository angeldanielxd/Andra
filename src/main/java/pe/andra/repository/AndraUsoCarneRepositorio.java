package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraUsoCarne;

import java.util.List;

@Repository
public interface AndraUsoCarneRepositorio extends JpaRepository<AndraUsoCarne, Long> {

    List<AndraUsoCarne> findAllByOrderByAndraFechaDesc();
}
