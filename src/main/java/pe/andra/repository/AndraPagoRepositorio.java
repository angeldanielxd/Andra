package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraPago;
import pe.andra.model.AndraPersona;

import java.util.List;
import java.util.Optional;

@Repository
public interface AndraPagoRepositorio extends JpaRepository<AndraPago, Long> {

    Optional<AndraPago> findByAndraCodigo(String andraCodigo);

    List<AndraPago> findAllByOrderByAndraCreadoDesc();

    List<AndraPago> findByAndraPersonaOrderByAndraCreadoDesc(AndraPersona andraPersona);

    long countByAndraBoletaIsNotNull();
}
