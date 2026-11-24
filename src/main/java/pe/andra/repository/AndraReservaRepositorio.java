package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.AndraEspacio;
import pe.andra.model.AndraPersona;
import pe.andra.model.AndraReserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AndraReservaRepositorio extends JpaRepository<AndraReserva, Long> {

    Optional<AndraReserva> findByAndraCodigo(String andraCodigo);

    List<AndraReserva> findAllByOrderByAndraIdDesc();

    List<AndraReserva> findByAndraPersonaOrderByAndraIdDesc(AndraPersona andraPersona);

    List<AndraReserva> findByAndraEspacioAndAndraFecha(AndraEspacio andraEspacio, LocalDate andraFecha);
}
