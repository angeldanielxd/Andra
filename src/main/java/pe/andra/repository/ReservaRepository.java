package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.Espacio;
import pe.andra.model.Persona;
import pe.andra.model.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findByCodigo(String codigo);

    List<Reserva> findAllByOrderByIdDesc();

    List<Reserva> findByPersonaOrderByIdDesc(Persona persona);

    List<Reserva> findByEspacioAndFecha(Espacio espacio, LocalDate fecha);
}
