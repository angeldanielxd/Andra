package pe.andra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.andra.model.AndraAjustes;
import pe.andra.repository.AndraAjustesRepositorio;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AndraAjustesServicio {

    private final AndraAjustesRepositorio andraRepositorio;

    @Transactional(readOnly = true)
    public AndraAjustes andraObtener() {
        return andraRepositorio.findById(1L).orElseGet(() -> AndraAjustes.builder().andraId(1L).andraIdioma("es").andraSede("central")
                .andraAvisoWhatsapp(true).andraAvisoCorreo(true).andraAvisoAplicacion(true).andraVersion(5).build());
    }

    @Transactional
    public AndraAjustes andraCambiar(Consumer<AndraAjustes> andraCambio) {
        AndraAjustes andraAjustes = andraObtener();
        andraCambio.accept(andraAjustes);
        return andraRepositorio.save(andraAjustes);
    }
}
