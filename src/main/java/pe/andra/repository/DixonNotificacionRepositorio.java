package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonNotificacion;
import pe.andra.model.DixonRol;

import java.util.List;

@Repository
public interface DixonNotificacionRepositorio extends JpaRepository<DixonNotificacion, Long> {

    List<DixonNotificacion> findByDixonDestinatarioOrderByDixonIdDesc(DixonRol dixonDestinatario);

    long countByDixonDestinatarioAndDixonLeidaFalse(DixonRol dixonDestinatario);
}
