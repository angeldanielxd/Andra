package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonEscuelaProfesional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonEscuelaProfesionalRepositorio extends JpaRepository<DixonEscuelaProfesional, Long> {

    Optional<DixonEscuelaProfesional> findByDixonCodigo(String dixonCodigo);

    List<DixonEscuelaProfesional> findAllByOrderByDixonIdAsc();
}
