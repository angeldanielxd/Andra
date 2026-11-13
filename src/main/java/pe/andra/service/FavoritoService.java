package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.Favorito;
import pe.andra.model.TipoFavorito;
import pe.andra.repository.FavoritoRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository repositorio;
    private final ConexionService conexion;

    @Transactional(readOnly = true)
    public boolean es(TipoFavorito tipo, String codigo) {
        return repositorio.existsByTipoAndCodigo(tipo, codigo);
    }

    @Transactional(readOnly = true)
    public Set<String> codigos(TipoFavorito tipo) {
        return repositorio.findByTipoOrderByIdAsc(tipo).stream().map(Favorito::getCodigo)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    public List<String> lista(TipoFavorito tipo) {
        return repositorio.findByTipoOrderByIdAsc(tipo).stream().map(Favorito::getCodigo).toList();
    }

    @Transactional
    public boolean alternar(TipoFavorito tipo, String codigo) {
        conexion.encolar("Favoritos actualizados");
        return repositorio.findByTipoAndCodigo(tipo, codigo).map(f -> {
            repositorio.delete(f);
            return false;
        }).orElseGet(() -> {
            repositorio.save(Favorito.builder().tipo(tipo).codigo(codigo).build());
            return true;
        });
    }
}
