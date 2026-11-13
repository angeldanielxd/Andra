package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.Notificacion;
import pe.andra.model.Rol;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByDestinatarioOrderByIdDesc(Rol destinatario);

    long countByDestinatarioAndLeidaFalse(Rol destinatario);
}
