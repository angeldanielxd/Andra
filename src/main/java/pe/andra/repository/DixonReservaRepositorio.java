package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonEspacio;
import pe.andra.model.DixonPersona;
import pe.andra.model.DixonReserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DixonReservaRepositorio extends JpaRepository<DixonReserva, Long> {

    Optional<DixonReserva> findByDixonCodigo(String dixonCodigo);

    List<DixonReserva> findAllByOrderByDixonIdDesc();

    List<DixonReserva> findByDixonPersonaOrderByDixonIdDesc(DixonPersona dixonPersona);

    List<DixonReserva> findByDixonEspacioAndDixonFecha(DixonEspacio dixonEspacio, LocalDate dixonFecha);
}
