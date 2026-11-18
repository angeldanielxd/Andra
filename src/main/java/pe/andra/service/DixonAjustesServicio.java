package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.DixonAjustes;
import pe.andra.repository.DixonAjustesRepositorio;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class DixonAjustesServicio {

    private final DixonAjustesRepositorio dixonRepositorio;

    @Transactional(readOnly = true)
    public DixonAjustes dixonObtener() {
        return dixonRepositorio.findById(1L).orElseGet(() -> DixonAjustes.builder().dixonId(1L).dixonIdioma("es").dixonSede("central")
                .dixonAvisoWhatsapp(true).dixonAvisoCorreo(true).dixonAvisoAplicacion(true).dixonVersion(5).build());
    }

    @Transactional
    public DixonAjustes dixonCambiar(Consumer<DixonAjustes> dixonCambio) {
        DixonAjustes dixonAjustes = dixonObtener();
        dixonCambio.accept(dixonAjustes);
        return dixonRepositorio.save(dixonAjustes);
    }
}
