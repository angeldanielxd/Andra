package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.AndraFavorito;
import pe.andra.model.AndraTipoFavorito;
import pe.andra.repository.AndraFavoritoRepositorio;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AndraFavoritoServicio {

    private final AndraFavoritoRepositorio andraRepositorio;
    private final AndraConexionServicio andraConexion;

    @Transactional(readOnly = true)
    public boolean andraEs(AndraTipoFavorito andraTipo, String andraCodigo) {
        return andraRepositorio.existsByAndraTipoAndAndraCodigo(andraTipo, andraCodigo);
    }

    @Transactional(readOnly = true)
    public Set<String> andraCodigos(AndraTipoFavorito andraTipo) {
        return andraRepositorio.findByAndraTipoOrderByAndraIdAsc(andraTipo).stream().map(AndraFavorito::getAndraCodigo)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    public List<String> andraLista(AndraTipoFavorito andraTipo) {
        return andraRepositorio.findByAndraTipoOrderByAndraIdAsc(andraTipo).stream().map(AndraFavorito::getAndraCodigo).toList();
    }

    @Transactional
    public boolean andraAlternar(AndraTipoFavorito andraTipo, String andraCodigo) {
        andraConexion.andraEncolar("Favoritos actualizados");
        return andraRepositorio.findByAndraTipoAndAndraCodigo(andraTipo, andraCodigo).map(andraFavorito -> {
            andraRepositorio.delete(andraFavorito);
            return false;
        }).orElseGet(() -> {
            andraRepositorio.save(AndraFavorito.builder().andraTipo(andraTipo).andraCodigo(andraCodigo).build());
            return true;
        });
    }
}
