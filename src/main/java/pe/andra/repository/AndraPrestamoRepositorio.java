package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraPersona;
import pe.andra.model.AndraPrestamo;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraPrestamoRepositorio extends JpaRepository<AndraPrestamo, Long> {

    Optional<AndraPrestamo> findByAndraClave(String andraClave);

    List<AndraPrestamo> findAllByOrderByAndraIdDesc();

    List<AndraPrestamo> findByAndraPersonaOrderByAndraIdDesc(AndraPersona andraPersona);

    boolean existsByAndraCodigo(String andraCodigo);
}
