package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.UsoCarne;

import java.util.List;

@Repository
public interface UsoCarneRepository extends JpaRepository<UsoCarne, Long> {

    List<UsoCarne> findAllByOrderByFechaDesc();
}
