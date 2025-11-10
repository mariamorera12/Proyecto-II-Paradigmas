package cr.ac.una.agenda.controller;

import cr.ac.una.agenda.dto.PrioridadRequest;
import cr.ac.una.agenda.entity.Prioridad;
import cr.ac.una.agenda.service.PrioridadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prioridades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrioridadController {

    private final PrioridadService prioridadService;

    @GetMapping
    public ResponseEntity<List<Prioridad>> obtenerPrioridades() {
        return ResponseEntity.ok(prioridadService.obtenerPrioridades());
    }

    @PostMapping
    public ResponseEntity<Prioridad> crearPrioridad(@RequestBody PrioridadRequest request) {
        return ResponseEntity.ok(prioridadService.crearPrioridad(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prioridad> actualizarPrioridad(
            @PathVariable Long id,
            @RequestBody PrioridadRequest request) {
        return ResponseEntity.ok(prioridadService.actualizarPrioridad(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPrioridad(@PathVariable Long id) {
        prioridadService.eliminarPrioridad(id);
        return ResponseEntity.noContent().build();
    }
}