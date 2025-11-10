package cr.ac.una.agenda.service;

import cr.ac.una.agenda.dto.ClimaRequest;
import cr.ac.una.agenda.entity.Clima;
import cr.ac.una.agenda.repository.ClimaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClimaService {

    private final ClimaRepository climaRepository;

    public List<Clima> obtenerClimas() {
        return climaRepository.findAll();
    }

    public Clima crearClima(ClimaRequest request) {
        Clima clima = new Clima();
        clima.setTipo(request.getTipo());
        return climaRepository.save(clima);
    }

    public Clima actualizarClima(Long id, ClimaRequest request) {
        Clima clima = climaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clima no encontrado con id: " + id));

        clima.setTipo(request.getTipo());
        return climaRepository.save(clima);
    }

    public void eliminarClima(Long id) {
        if (!climaRepository.existsById(id)) {
            throw new RuntimeException("Clima no encontrado con id: " + id);
        }
        climaRepository.deleteById(id);
    }
}