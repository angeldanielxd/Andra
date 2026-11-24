package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraCambioPendiente;

import java.util.List;

@Repository
public interface AndraCambioPendienteRepositorio extends JpaRepository<AndraCambioPendiente, Long> {

    List<AndraCambioPendiente> findAllByOrderByAndraIdAsc();
}
