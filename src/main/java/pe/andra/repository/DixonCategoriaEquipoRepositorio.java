package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonCategoriaEquipo;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonCategoriaEquipoRepositorio extends JpaRepository<DixonCategoriaEquipo, Long> {

    Optional<DixonCategoriaEquipo> findByDixonCodigo(String dixonCodigo);

    List<DixonCategoriaEquipo> findAllByOrderByDixonIdAsc();
}
