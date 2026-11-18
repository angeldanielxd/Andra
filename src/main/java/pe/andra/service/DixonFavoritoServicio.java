package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.DixonFavorito;
import pe.andra.model.DixonTipoFavorito;
import pe.andra.repository.DixonFavoritoRepositorio;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DixonFavoritoServicio {

    private final DixonFavoritoRepositorio dixonRepositorio;
    private final DixonConexionServicio dixonConexion;

    @Transactional(readOnly = true)
    public boolean dixonEs(DixonTipoFavorito dixonTipo, String dixonCodigo) {
        return dixonRepositorio.existsByDixonTipoAndDixonCodigo(dixonTipo, dixonCodigo);
    }

    @Transactional(readOnly = true)
    public Set<String> dixonCodigos(DixonTipoFavorito dixonTipo) {
        return dixonRepositorio.findByDixonTipoOrderByDixonIdAsc(dixonTipo).stream().map(DixonFavorito::getDixonCodigo)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    public List<String> dixonLista(DixonTipoFavorito dixonTipo) {
        return dixonRepositorio.findByDixonTipoOrderByDixonIdAsc(dixonTipo).stream().map(DixonFavorito::getDixonCodigo).toList();
    }

    @Transactional
    public boolean dixonAlternar(DixonTipoFavorito dixonTipo, String dixonCodigo) {
        dixonConexion.dixonEncolar("Favoritos actualizados");
        return dixonRepositorio.findByDixonTipoAndDixonCodigo(dixonTipo, dixonCodigo).map(dixonFavorito -> {
            dixonRepositorio.delete(dixonFavorito);
            return false;
        }).orElseGet(() -> {
            dixonRepositorio.save(DixonFavorito.builder().dixonTipo(dixonTipo).dixonCodigo(dixonCodigo).build());
            return true;
        });
    }
}
