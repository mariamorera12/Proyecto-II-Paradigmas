package cr.ac.una.agenda.controller;

import cr.ac.una.agenda.dto.*;
import cr.ac.una.agenda.service.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tareas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TareaController {

    private final TareaService tareaService;

    @PostMapping
    public ResponseEntity<TareaResponse> crearTarea(@RequestBody TareaRequest request) {
        return ResponseEntity.ok(tareaService.crearTarea(request));
    }

    @GetMapping
    public ResponseEntity<List<TareaResponse>> obtenerTodasLasTareas() {
        return ResponseEntity.ok(tareaService.obtenerTodasLasTareas());
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<TareaResponse>> obtenerTareasPorFecha(@PathVariable String fecha) {
        LocalDate localDate = LocalDate.parse(fecha);
        return ResponseEntity.ok(tareaService.obtenerTareasPorFecha(localDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaResponse> actualizarTarea(
            @PathVariable Long id,
            @RequestBody TareaRequest request) {
        return ResponseEntity.ok(tareaService.actualizarTarea(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/completar")
    public ResponseEntity<Void> marcarComoCompletada(@PathVariable Long id) {
        tareaService.marcarComoCompletada(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/planificar")
    public ResponseEntity<PlanificacionResponse> planificarDia(
            @RequestBody PlanificacionRequest request) {
        return ResponseEntity.ok(tareaService.planificarDia(request));
    }
}