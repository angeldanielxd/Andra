package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonUsoCarne;

import java.util.List;

@Repository
public interface DixonUsoCarneRepositorio extends JpaRepository<DixonUsoCarne, Long> {

    List<DixonUsoCarne> findAllByOrderByDixonFechaDesc();
}
