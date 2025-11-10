package cr.ac.una.agenda.service;

import cr.ac.una.agenda.dto.PrioridadRequest;
import cr.ac.una.agenda.entity.Prioridad;
import cr.ac.una.agenda.repository.PrioridadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PrioridadService {

    private final PrioridadRepository prioridadRepository;

    public List<Prioridad> obtenerPrioridades() {
        return prioridadRepository.findAll();
    }

    public Prioridad crearPrioridad(PrioridadRequest request) {
        Prioridad prioridad = new Prioridad();
        prioridad.setNivel(request.getNivel());
        prioridad.setPeso(request.getPeso());
        return prioridadRepository.save(prioridad);
    }

    public Prioridad actualizarPrioridad(Long id, PrioridadRequest request) {
        Prioridad prioridad = prioridadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prioridad no encontrada con id: " + id));

        prioridad.setNivel(request.getNivel());
        prioridad.setPeso(request.getPeso());
        return prioridadRepository.save(prioridad);
    }

    public void eliminarPrioridad(Long id) {
        if (!prioridadRepository.existsById(id)) {
            throw new RuntimeException("Prioridad no encontrada con id: " + id);
        }
        prioridadRepository.deleteById(id);
    }
}