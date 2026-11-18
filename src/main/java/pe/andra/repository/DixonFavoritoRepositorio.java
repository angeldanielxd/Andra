package pe.andra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.andra.model.DixonFavorito;
import pe.andra.model.DixonTipoFavorito;

import java.util.List;
import java.util.Optional;

@Repository
public interface DixonFavoritoRepositorio extends JpaRepository<DixonFavorito, Long> {

    List<DixonFavorito> findByDixonTipoOrderByDixonIdAsc(DixonTipoFavorito dixonTipo);

    Optional<DixonFavorito> findByDixonTipoAndDixonCodigo(DixonTipoFavorito dixonTipo, String dixonCodigo);

    boolean existsByDixonTipoAndDixonCodigo(DixonTipoFavorito dixonTipo, String dixonCodigo);
}
